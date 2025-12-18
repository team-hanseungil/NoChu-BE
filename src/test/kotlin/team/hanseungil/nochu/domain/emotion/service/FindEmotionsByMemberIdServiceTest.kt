package team.hanseungil.nochu.domain.emotion.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.emotion.repository.projection.EmotionProjection
import java.time.LocalDate
import java.time.LocalDateTime

class FindEmotionsByMemberIdServiceTest : BehaviorSpec({
    Given("멤버의 감정 기록이 존재할 때") {
        When("멤버 ID로 감정 목록을 조회하면") {
            Then("감정 목록과 통계 정보를 반환한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository)

                val memberId = 1L
                val emotions = listOf(
                    createEmotionProjection(1L, "행복", LocalDateTime.now(), mapOf("행복" to 0.8)),
                    createEmotionProjection(2L, "슬픔", LocalDateTime.now().minusDays(1), mapOf("슬픔" to 0.7)),
                    createEmotionProjection(3L, "분노", LocalDateTime.now().minusDays(2), mapOf("분노" to 0.6))
                )

                every { emotionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId) } returns emotions

                val result = service.execute(memberId)

                result.totalRecords shouldBe 3
                result.averageConfidence shouldBe 70
                result.streak shouldBe 3
                result.emotions.size shouldBe 3
                result.emotions[0].emotion shouldBe "행복"
                result.emotions[0].confidence shouldBe 80
            }
        }
    }

    Given("멤버의 감정 기록이 없을 때") {
        When("멤버 ID로 감정 목록을 조회하면") {
            Then("빈 목록과 0인 통계 정보를 반환한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository)

                val memberId = 1L

                every { emotionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId) } returns emptyList()

                val result = service.execute(memberId)

                result.totalRecords shouldBe 0
                result.averageConfidence shouldBe 0
                result.streak shouldBe 0
                result.emotions.size shouldBe 0
            }
        }
    }

    Given("연속되지 않은 감정 기록이 있을 때") {
        When("멤버 ID로 감정 목록을 조회하면") {
            Then("streak가 0으로 계산된다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository)

                val memberId = 1L
                val emotions = listOf(
                    createEmotionProjection(1L, "행복", LocalDateTime.now().minusDays(3), mapOf("행복" to 0.8)),
                    createEmotionProjection(2L, "슬픔", LocalDateTime.now().minusDays(4), mapOf("슬픔" to 0.7))
                )

                every { emotionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId) } returns emotions

                val result = service.execute(memberId)

                result.streak shouldBe 0
            }
        }
    }

    Given("오늘 기록이 있을 때") {
        When("오늘과 어제 연속 기록이 있으면") {
            Then("streak가 2로 계산된다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository)

                val memberId = 1L
                val emotions = listOf(
                    createEmotionProjection(1L, "행복", LocalDateTime.now(), mapOf("행복" to 0.8)),
                    createEmotionProjection(2L, "슬픔", LocalDateTime.now().minusDays(1), mapOf("슬픔" to 0.7))
                )

                every { emotionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId) } returns emotions

                val result = service.execute(memberId)

                result.streak shouldBe 2
            }
        }
    }
})

private fun createEmotionProjection(
    id: Long,
    emotion: String,
    createdAt: LocalDateTime,
    emotions: Map<String, Double>
): EmotionProjection {
    return object : EmotionProjection {
        override val id = id
        override val emotion = emotion
        override val createdAt = createdAt
        override val emotions = emotions
    }
}
