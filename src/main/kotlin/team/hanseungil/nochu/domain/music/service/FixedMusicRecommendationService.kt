package team.hanseungil.nochu.domain.music.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.music.entity.Music
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.entity.Playlist
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistResponse
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.webclient.spotify.client.SpotifyWebClient
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifySearchResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.LinkedHashMap

@Service
class FixedMusicRecommendationService(
    private val musicJpaRepository: MusicJpaRepository,
    private val playlistJpaRepository: PlaylistJpaRepository,
    private val spotifyWebClient: SpotifyWebClient,
    private val emotionJpaRepository: EmotionJpaRepository,
    private val objectMapper: ObjectMapper,
    private val transactionTemplate: TransactionTemplate,
) {

    companion object {
        private const val MIN_POPULARITY = 15
        private const val POPULARITY_DELTA = 10
        private const val MAX_POPULARITY = 85

        private const val YEAR_FROM = 2014
        private const val YEAR_TO = 2025

        private const val MAX_TRACKS = 10

        private const val MAX_OFFSET = 1000
        private const val SPOTIFY_LIMIT = 50
        private const val OFFSET_STEP = SPOTIFY_LIMIT
        private const val PAGES_TO_POOL = 10
    }

    suspend fun execute(memberId: Long, keyword: String): PlaylistResponse = coroutineScope {
        val (startOfDay, endOfDay) = todayRange()

        val latestEmotion = emotionJpaRepository
            .findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                memberId = memberId,
                startDate = startOfDay,
                endDate = endOfDay,
            ) ?: throw GlobalException(ErrorCode.EMOTION_NOT_FOUND)

        val strictQuery = "${keyword} year:${YEAR_FROM}-${YEAR_TO}"

        val picked = LinkedHashMap<String, SpotifySearchResponse.Track>()

        suspend fun collect(
            keywords: String,
            onlyPopular: Boolean,
            strictYearFilter: Boolean,
        ) {
            var offset = 0
            var emptyPages = 0

            val pool = ArrayList<SpotifySearchResponse.Track>(SPOTIFY_LIMIT * PAGES_TO_POOL)
            var pagesCollected = 0

            while (
                picked.size < MAX_TRACKS &&
                offset <= (MAX_OFFSET - SPOTIFY_LIMIT) &&
                pagesCollected < PAGES_TO_POOL
            ) {
                val response = spotifyWebClient.searchTrackByKeywords(
                    keywords = keywords,
                    offset = offset,
                )

                val items = response.tracks.items

                if (items.isEmpty()) {
                    emptyPages += 1
                    if (emptyPages >= 2) break
                    offset += OFFSET_STEP
                    continue
                }

                emptyPages = 0

                val base = if (strictYearFilter) {
                    items.filter { inYearRange(it) }
                } else {
                    items
                }

                pool.addAll(base)
                pagesCollected += 1

                if (items.size < SPOTIFY_LIMIT) break

                offset += OFFSET_STEP
            }

            val filtered = if (onlyPopular) {
                val threshold = computeDynamicPopularityThreshold(pool)
                pool.filter { (it.popularity ?: 0) >= threshold }
            } else {
                pool
            }

            for (t in filtered) {
                if (picked.size >= MAX_TRACKS) break
                picked.putIfAbsent(t.id, t)
            }
        }

        // 1) 엄격 + 인기 필터
        collect(keywords = strictQuery, onlyPopular = true, strictYearFilter = true)

        // 2) 엄격 + 인기 필터 제거
        if (picked.size < MAX_TRACKS) {
            collect(keywords = strictQuery, onlyPopular = false, strictYearFilter = true)
        }

        // 3) 연도 필터도 제거(더 완화)
        if (picked.size < MAX_TRACKS) {
            collect(keywords = keyword, onlyPopular = false, strictYearFilter = false)
        }

        val finalItems = picked.values.take(MAX_TRACKS)

        if (finalItems.isEmpty()) {
            throw GlobalException(ErrorCode.MUSIC_NOT_FOUND)
        }

        val playlistImageUrl = finalItems
            .firstOrNull()
            ?.album
            ?.images
            ?.firstOrNull()
            ?.url

        // 저장용으로 응답 형태 맞추기
        val firstResponse = spotifyWebClient.searchTrackByKeywords(keywords = strictQuery)
        val filteredSpotifyResponse = firstResponse.copy(
            tracks = firstResponse.tracks.copy(
                items = finalItems,
            ),
        )

        savePlaylistWithMusics(
            latestEmotion = latestEmotion,
            title = keyword,
            imageUrl = playlistImageUrl,
            spotifyResponse = filteredSpotifyResponse,
        )
    }

    private fun savePlaylistWithMusics(
        latestEmotion: Emotion,
        title: String,
        imageUrl: String?,
        spotifyResponse: SpotifySearchResponse
    ): PlaylistResponse {
        return transactionTemplate.execute {
            val playlist = playlistJpaRepository.save(
                Playlist(
                    emotion = latestEmotion,
                    title = title,
                    imageUrl = imageUrl
                ),
            )

            val musics = spotifyResponse.tracks.items.mapIndexed { index, track ->
                Music(
                    title = track.name,
                    artist = objectMapper.writeValueAsString(
                        track.artists.map { it.name }.ifEmpty { listOf("Unknown") },
                    ),
                    album = track.album.name,
                    durationMs = track.durationMs,
                    spotifyId = track.id,
                    spotifyUrl = track.externalUrls?.spotify ?: toSpotifyTrackUrl(track.id),
                    sortOrder = index,
                    playlist = playlist,
                    imageUrl = track.album.images?.firstOrNull()?.url,
                )
            }

            val savedMusics = musicJpaRepository.saveAll(musics)

            val tracks = savedMusics
                .sortedWith(compareBy<Music> { it.sortOrder }.thenBy { it.id })
                .mapIndexed { index, music ->
                    val spotifyTrack = spotifyResponse.tracks.items.getOrNull(index)
                    PlaylistResponse.Track(
                        artists = parseArtists(music.artist),
                        title = music.title,
                        imageUrl = spotifyTrack?.album?.images?.firstOrNull()?.url,
                        spotifyUrl = spotifyTrack?.externalUrls?.spotify,
                        duration = formatDuration(music.durationMs ?: 0)
                    )
                }

            PlaylistResponse(
                id = playlist.id,
                title = title,
                imageUrl = imageUrl,
                tracks = tracks
            )
        }
    }

    private fun extractReleaseYear(track: SpotifySearchResponse.Track): Int? {
        val releaseDate = track.album.releaseDate ?: return null
        val yearStr = releaseDate.take(4)
        return yearStr.toIntOrNull()
    }

    private fun inYearRange(track: SpotifySearchResponse.Track): Boolean {
        val y = extractReleaseYear(track) ?: return false
        return y in YEAR_FROM..YEAR_TO
    }

    private fun computeDynamicPopularityThreshold(tracks: List<SpotifySearchResponse.Track>): Int {
        if (tracks.isEmpty()) return MIN_POPULARITY

        val pops = tracks.map { it.popularity ?: 0 }
        val mean = pops.average()

        val raw = kotlin.math.round(mean).toInt() + POPULARITY_DELTA
        return raw.coerceIn(MIN_POPULARITY, MAX_POPULARITY)
    }

    private fun parseArtists(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return listOf("Unknown")

        return try {
            val arr = objectMapper.readValue(raw, Array<String>::class.java)
            arr.toList().filter { it.isNotBlank() }.ifEmpty { listOf("Unknown") }
        } catch (_: Exception) {
            listOf(raw)
        }
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val start = LocalDate.now().atStartOfDay()
        return start to start.plusDays(1)
    }

    private fun toSpotifyTrackUrl(spotifyId: String): String =
        "https://open.spotify.com/track/$spotifyId"
}