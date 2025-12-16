package team.hanseungil.nochu.domain.playlist.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.music.entity.Music
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.entity.Playlist
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.util.*

class PlaylistDetailServiceTest : BehaviorSpec({
    Given("플레이리스트 ID가 주어졌을 때") {
        val playlistJpaRepository = mockk<PlaylistJpaRepository>()
        val musicJpaRepository = mockk<MusicJpaRepository>()
        
        val service = PlaylistDetailService(
            playlistJpaRepository,
            musicJpaRepository
        )

        val emotion = Emotion(
            id = 1L,
            emotions = mapOf("happy" to 0.8),
            imageUrl = "https://test.com/image.jpg",
            emotion = "happy",
            memberId = 1L
        )

        val playlist = Playlist(
            id = 1L,
            emotion = emotion,
            title = "행복한 플레이리스트",
            imageUrl = "https://test.com/playlist.jpg"
        )

        val musics = listOf(
            Music(
                id = 1L,
                title = "Happy Song 1",
                artist = "Artist 1",
                album = "Album 1",
                durationMs = 180000,
                spotifyId = "track1",
                spotifyUrl = "https://spotify.com/track1",
                imageUrl = "https://test.com/album1.jpg",
                sortOrder = 0,
                playlist = playlist
            ),
            Music(
                id = 2L,
                title = "Happy Song 2",
                artist = "Artist 2",
                album = "Album 2",
                durationMs = 240000,
                spotifyId = "track2",
                spotifyUrl = "https://spotify.com/track2",
                imageUrl = "https://test.com/album2.jpg",
                sortOrder = 1,
                playlist = playlist
            )
        )

        every { playlistJpaRepository.findById(1L) } returns Optional.of(playlist)
        every { musicJpaRepository.findAllByPlaylistIdOrderBySortOrderAsc(1L) } returns musics

        When("플레이리스트 상세 조회를 실행하면") {
            val result = service.execute(1L)

            Then("플레이리스트 정보와 트랙 목록을 반환한다") {
                result.id shouldBe 1L
                result.title shouldBe "행복한 플레이리스트"
                result.imageUrl shouldBe "https://test.com/playlist.jpg"
                result.tracks shouldHaveSize 2
                
                result.tracks[0].title shouldBe "Happy Song 1"
                result.tracks[0].artists shouldBe listOf("Artist 1")
                result.tracks[0].duration shouldBe "3:00"
                result.tracks[0].imageUrl shouldBe "https://test.com/album1.jpg"
                result.tracks[0].spotifyUrl shouldBe "https://spotify.com/track1"
                
                result.tracks[1].title shouldBe "Happy Song 2"
                result.tracks[1].duration shouldBe "4:00"
            }
        }
    }

    Given("존재하지 않는 플레이리스트 ID가 주어졌을 때") {
        val playlistJpaRepository = mockk<PlaylistJpaRepository>()
        val musicJpaRepository = mockk<MusicJpaRepository>()
        
        val service = PlaylistDetailService(
            playlistJpaRepository,
            musicJpaRepository
        )

        every { playlistJpaRepository.findById(999L) } returns Optional.empty()

        When("플레이리스트 상세 조회를 실행하면") {
            Then("PLAYLIST_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    service.execute(999L)
                }
                exception.errorCode shouldBe ErrorCode.PLAYLIST_NOT_FOUND
            }
        }
    }
})
