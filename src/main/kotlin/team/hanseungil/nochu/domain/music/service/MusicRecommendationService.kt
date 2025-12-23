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
import team.hanseungil.nochu.infrastructure.webclient.music.client.MusicWebClient
import team.hanseungil.nochu.infrastructure.webclient.music.dto.request.MusicKeywordRequest
import team.hanseungil.nochu.infrastructure.webclient.spotify.client.SpotifyWebClient
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifySearchResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.LinkedHashMap

@Service
class MusicRecommendationService(
    private val musicJpaRepository: MusicJpaRepository,
    private val playlistJpaRepository: PlaylistJpaRepository,
    private val spotifyWebClient: SpotifyWebClient,
    private val musicWebClient: MusicWebClient,
    private val emotionJpaRepository: EmotionJpaRepository,
    private val objectMapper: ObjectMapper,
    private val transactionTemplate: TransactionTemplate,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(MusicRecommendationService::class.java)

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

    suspend fun execute(memberId: Long): PlaylistResponse = coroutineScope {
        log.info("MusicRecommendation start memberId={}", memberId)
        val (startOfDay, endOfDay) = todayRange()

        val latestEmotion = emotionJpaRepository
            .findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                memberId = memberId,
                startDate = startOfDay,
                endDate = endOfDay,
            ) ?: throw GlobalException(ErrorCode.EMOTION_NOT_FOUND)
        log.debug("Latest emotion fetched memberId={} emotionId={} createdAt={}", memberId, latestEmotion.id, latestEmotion.createdAt)

        val keywordResponse = musicWebClient.extractKeywords(
            request = MusicKeywordRequest(
                happy = latestEmotion.emotions["행복"] ?: 0.0,
                surprise = latestEmotion.emotions["당황"] ?: 0.0,
                anger = latestEmotion.emotions["분노"] ?: 0.0,
                anxiety = latestEmotion.emotions["불안"] ?: 0.0,
                hurt = latestEmotion.emotions["상처"] ?: 0.0,
                sad = latestEmotion.emotions["슬픔"] ?: 0.0,

            ),
        )
        log.info("Keyword extracted memberId={} title='{}' keywords='{}'", memberId, keywordResponse.title, keywordResponse.keywords)

        if (keywordResponse.keywords.isBlank()) {
            log.warn("Empty keywords memberId={}", memberId)
            throw GlobalException(ErrorCode.MUSIC_NOT_FOUND)
        }

        val strictQuery = "${keywordResponse.keywords} year:${YEAR_FROM}-${YEAR_TO}"

        val picked = LinkedHashMap<String, SpotifySearchResponse.Track>()

        suspend fun collect(
            keywords: String,
            onlyPopular: Boolean,
            strictYearFilter: Boolean,
        ) {
            log.debug("Collect start keywords='{}' onlyPopular={} strictYearFilter={}", keywords, onlyPopular, strictYearFilter)
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
                log.debug("Spotify search page offset={} returned={}", offset, response.tracks.items.size)

                val items = response.tracks.items

                if (items.isEmpty()) {
                    emptyPages += 1
                    if (emptyPages >= 2) {
                        log.warn("Empty pages threshold reached keywords='{}' offset={}", keywords, offset)
                        break
                    }
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
            log.debug("Filtered tracks count={} onlyPopular={}", filtered.size, onlyPopular)

            for (t in filtered) {
                if (picked.size >= MAX_TRACKS) break
                picked.putIfAbsent(t.id, t)
            }
        }

        collect(keywords = strictQuery, onlyPopular = true, strictYearFilter = true)

        if (picked.size < MAX_TRACKS) {
            collect(keywords = strictQuery, onlyPopular = false, strictYearFilter = true)
        }

        if (picked.size < MAX_TRACKS) {
            val relaxedKeywords = keywordResponse.keywords.trim()
            if (relaxedKeywords.isNotBlank()) {
                collect(keywords = relaxedKeywords, onlyPopular = false, strictYearFilter = false)
            }
        }
        log.info("Tracks picked count={}", picked.size)

        val finalItems = picked.values.take(MAX_TRACKS)

        if (finalItems.isEmpty()) {
            log.error("No tracks selected after collection memberId={}", memberId)
            throw GlobalException(ErrorCode.MUSIC_NOT_FOUND)
        }

        val playlistImageUrl = finalItems
            .firstOrNull()
            ?.album
            ?.images
            ?.firstOrNull()
            ?.url

        val firstResponse = spotifyWebClient.searchTrackByKeywords(keywords = strictQuery)
        val filteredSpotifyResponse = firstResponse.copy(
            tracks = firstResponse.tracks.copy(
                items = finalItems,
            ),
        )

        savePlaylistWithMusics(latestEmotion, keywordResponse.title, playlistImageUrl, filteredSpotifyResponse)
    }

    private fun savePlaylistWithMusics(
        latestEmotion: Emotion,
        title: String,
        imageUrl: String?,
        spotifyResponse: SpotifySearchResponse
    ): PlaylistResponse {
        log.info("Saving playlist title='{}'", title)
        return transactionTemplate.execute {
            val playlist = playlistJpaRepository.save(
                Playlist(
                    emotion = latestEmotion,
                    title = title,
                    imageUrl = imageUrl
                ),
            )
            log.info("Playlist saved id={}", playlist.id)

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
            log.info("Musics saved count={}", savedMusics.size)

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
        val mean = pops.average() // Double

        val raw = kotlin.math.round(mean).toInt() + POPULARITY_DELTA
        val threshold = raw.coerceIn(MIN_POPULARITY, MAX_POPULARITY)

        return threshold
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