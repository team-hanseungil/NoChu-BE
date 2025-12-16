package team.hanseungil.nochu.domain.playlist.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.entity.Playlist
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import java.time.LocalDateTime

class PlaylistListServiceTest : BehaviorSpec({

    val playlistJpaRepository = mockk<PlaylistJpaRepository>()
    val musicJpaRepository = mockk<MusicJpaRepository>()

    Given("멤버 ID로 플레이리스트 목록을 조회할 때") {
        val memberId = 1L
        val emotion1 = Emotion(
            id = 1L,
            emotions = mapOf("happy" to 0.8),
            imageUrl = "https://test.com/image1.jpg",
            emotion = "happy",
            memberId = memberId
        )
        emotion1.createdAt = LocalDateTime.of(2025, 12, 15, 14, 0, 0)

        val emotion2 = Emotion(
            id = 2L,
            emotions = mapOf("sad" to 0.7),
            imageUrl = "https://test.com/image2.jpg",
            emotion = "sad",
            memberId = memberId
        )
        emotion2.createdAt = LocalDateTime.of(2025, 12, 14, 10, 0, 0)

        val playlist1 = Playlist(
            id = 1L,
            emotion = emotion1,
            title = "행복한 플레이리스트"
        )

        val playlist2 = Playlist(
            id = 2L,
            emotion = emotion2,
            title = "슬픈 플레이리스트"
        )

        every { playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns listOf(playlist1, playlist2)

        val musicJpaRepository = mockk<MusicJpaRepository>()
        val serviceWithMusicRepo = PlaylistListService(playlistJpaRepository, musicJpaRepository)

        every { musicJpaRepository.countByPlaylistId(1L) } returns 5L
        every { musicJpaRepository.countByPlaylistId(2L) } returns 3L

        When("플레이리스트 목록을 조회하면") {
            Then("최신순으로 정렬된 플레이리스트 목록을 반환한다") {
                val result = serviceWithMusicRepo.execute(memberId)

                result.playlists.size shouldBe 2
                result.playlists[0].id shouldBe 1L
                result.playlists[0].title shouldBe "행복한 플레이리스트"
                result.playlists[0].emotion shouldBe "happy"
                result.playlists[0].imageUrl shouldBe null
                result.playlists[0].count shouldBe 5L
                result.playlists[0].createdAt shouldBe "2025-12-15T14:00:00"
                
                result.playlists[1].id shouldBe 2L
                result.playlists[1].title shouldBe "슬픈 플레이리스트"
                result.playlists[1].emotion shouldBe "sad"
                result.playlists[1].imageUrl shouldBe null
                result.playlists[1].count shouldBe 3L
                result.playlists[1].createdAt shouldBe "2025-12-14T10:00:00"

                verify(exactly = 1) { playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId) }
                verify(exactly = 1) { musicJpaRepository.countByPlaylistId(1L) }
                verify(exactly = 1) { musicJpaRepository.countByPlaylistId(2L) }
            }
        }
    }

    Given("플레이리스트가 없는 멤버 ID로 조회할 때") {
        val memberId = 999L
        val musicJpaRepository = mockk<MusicJpaRepository>()
        val serviceWithMusicRepo = PlaylistListService(playlistJpaRepository, musicJpaRepository)

        every { playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns emptyList()

        When("플레이리스트 목록을 조회하면") {
            Then("빈 목록을 반환한다") {
                val result = serviceWithMusicRepo.execute(memberId)

                result.playlists.size shouldBe 0
                verify(exactly = 1) { playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId) }
            }
        }
    }

    Given("트랙이 0개인 플레이리스트가 있을 때") {
        val memberId = 1L
        val emotion = Emotion(
            id = 1L,
            emotions = mapOf("happy" to 0.8),
            imageUrl = "https://test.com/image1.jpg",
            emotion = "happy",
            memberId = memberId
        )
        emotion.createdAt = LocalDateTime.of(2025, 12, 15, 14, 0, 0)

        val playlist = Playlist(
            id = 1L,
            emotion = emotion,
            title = "빈 플레이리스트"
        )

        every { playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId) } returns listOf(playlist)
        every { musicJpaRepository.countByPlaylistId(1L) } returns 0L

        val serviceWithMusicRepo = PlaylistListService(playlistJpaRepository, musicJpaRepository)

        When("플레이리스트 목록을 조회하면") {
            Then("count가 0인 플레이리스트를 반환한다") {
                val result = serviceWithMusicRepo.execute(memberId)

                result.playlists.size shouldBe 1
                result.playlists[0].count shouldBe 0L
                verify(exactly = 1) { musicJpaRepository.countByPlaylistId(1L) }
            }
        }
    }
})
