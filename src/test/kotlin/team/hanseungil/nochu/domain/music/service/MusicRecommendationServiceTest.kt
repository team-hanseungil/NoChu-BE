package team.hanseungil.nochu.domain.music.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.springframework.transaction.support.TransactionTemplate
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.music.entity.Music
import team.hanseungil.nochu.domain.playlist.entity.Playlist
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.webclient.music.client.MusicWebClient
import team.hanseungil.nochu.infrastructure.webclient.music.dto.response.MusicKeywordResponse
import team.hanseungil.nochu.infrastructure.webclient.spotify.client.SpotifyWebClient
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifySearchResponse
import java.time.LocalDateTime

class MusicRecommendationServiceTest : BehaviorSpec({
    Given("유효한 멤버 ID와 오늘의 감정 데이터가 존재할 때") {
        val musicJpaRepository = mockk<MusicJpaRepository>()
        val playlistJpaRepository = mockk<PlaylistJpaRepository>()
        val spotifyWebClient = mockk<SpotifyWebClient>()
        val musicWebClient = mockk<MusicWebClient>()
        val emotionJpaRepository = mockk<EmotionJpaRepository>()
        val objectMapper = ObjectMapper()
        val transactionTemplate = mockk<TransactionTemplate>()
        
        val service = MusicRecommendationService(
            musicJpaRepository,
            playlistJpaRepository,
            spotifyWebClient,
            musicWebClient,
            emotionJpaRepository,
            objectMapper,
            transactionTemplate
        )

        val memberId = 1L
        val emotion = Emotion(
            id = 1L,
            emotions = mapOf("happy" to 0.8, "sad" to 0.2),
            imageUrl = "https://test.com/image.jpg",
            emotion = "happy",
            memberId = memberId
        )
        emotion.createdAt = LocalDateTime.now()

        val keywordResponse = MusicKeywordResponse(
            keywords = "happy upbeat energetic",
            title = "행복한 플레이리스트"
        )
        val spotifyResponse = SpotifySearchResponse(
            tracks = SpotifySearchResponse.Tracks(
                items = listOf(
                    SpotifySearchResponse.Track(
                        id = "track1",
                        name = "Happy Song",
                        artists = listOf(SpotifySearchResponse.Artist("Artist 1")),
                        album = SpotifySearchResponse.Album(
                            name = "Album 1",
                            images = listOf(SpotifySearchResponse.Image("https://test.com/album1.jpg")),
                            releaseDate = "2020-01-01"
                        ),
                        durationMs = 180000,
                        externalUrls = SpotifySearchResponse.ExternalUrls("https://spotify.com/track1"),
                        popularity = 50
                    )
                )
            )
        )
        val playlist = Playlist(
            id = 1L,
            emotion = emotion,
            title = "행복한 플레이리스트",
            imageUrl = "https://test.com/album1.jpg"
        )

        val savedMusic = Music(
            id = 1L,
            title = "Happy Song",
            artist = "Artist 1",
            album = "Album 1",
            durationMs = 180000,
            spotifyId = "track1",
            spotifyUrl = "https://spotify.com/track1",
            imageUrl = "https://test.com/album1.jpg",
            sortOrder = 0,
            playlist = playlist
        )

        every { emotionJpaRepository.findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any()) } returns emotion
        coEvery { musicWebClient.extractKeywords(any()) } returns keywordResponse
        coEvery { spotifyWebClient.searchTrackByKeywords(any()) } returns spotifyResponse
        every { transactionTemplate.execute<Any>(any()) } answers {
            val callback = firstArg<org.springframework.transaction.support.TransactionCallback<Any>>()
            callback.doInTransaction(mockk())
        }
        every { playlistJpaRepository.save(any()) } returns playlist
        every { musicJpaRepository.saveAll(any<List<Music>>()) } returns listOf(savedMusic)

        When("음악 추천을 실행하면") {
            Then("PlaylistResponse를 반환한다") {
                runTest {
                    val result = service.execute(memberId)
                    result.id shouldBe 1L
                    result.title shouldBe "행복한 플레이리스트"
                    result.imageUrl shouldBe "https://test.com/album1.jpg"
                    result.tracks shouldHaveSize 1
                    result.tracks[0].title shouldBe "Happy Song"
                    result.tracks[0].artists shouldBe listOf("Artist 1")
                    result.tracks[0].duration shouldBe "3:00"
                    result.tracks[0].imageUrl shouldBe "https://test.com/album1.jpg"
                    result.tracks[0].spotifyUrl shouldBe "https://spotify.com/track1"
                }
            }
        }
    }

    Given("오늘의 감정 데이터가 없을 때") {
        val musicJpaRepository = mockk<MusicJpaRepository>()
        val playlistJpaRepository = mockk<PlaylistJpaRepository>()
        val spotifyWebClient = mockk<SpotifyWebClient>()
        val musicWebClient = mockk<MusicWebClient>()
        val emotionJpaRepository = mockk<EmotionJpaRepository>()
        val objectMapper = ObjectMapper()
        val transactionTemplate = mockk<TransactionTemplate>()
        
        val service = MusicRecommendationService(
            musicJpaRepository,
            playlistJpaRepository,
            spotifyWebClient,
            musicWebClient,
            emotionJpaRepository,
            objectMapper,
            transactionTemplate
        )

        every { emotionJpaRepository.findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any()) } returns null

        When("음악 추천을 실행하면") {
            Then("EMOTION_NOT_FOUND 예외가 발생한다") {
                runTest {
                    val exception = shouldThrow<GlobalException> {
                        service.execute(999L)
                    }
                    exception.errorCode shouldBe ErrorCode.EMOTION_NOT_FOUND
                }
            }
        }
    }
})
