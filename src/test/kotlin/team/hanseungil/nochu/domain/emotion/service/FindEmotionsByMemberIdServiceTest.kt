package team.hanseungil.nochu.domain.emotion.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.emotion.repository.EmotionProjection
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.time.LocalDate

class FindEmotionsByMemberIdServiceTest : BehaviorSpec({
    Given("존재하는 멤버 ID가 주어졌을 때") {
        When("감정 목록을 조회하면") {
            Then("감정 통계와 목록을 반환한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val memberJpaRepository = mockk<MemberJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository, memberJpaRepository)

                val memberId = 1L
                val emotions = listOf(
                    EmotionProjection(
                        id = 1L,
                        date = LocalDate.now(),
                        emotion = "행복",
                        confidence = 85L,
                    ),
                    EmotionProjection(
                        id = 2L,
                        date = LocalDate.now().minusDays(1),
                        emotion = "즐거움",
                        confidence = 90L,
                    ),
                    EmotionProjection(
                        id = 3L,
                        date = LocalDate.now().minusDays(2),
                        emotion = "평온",
                        confidence = 75L,
                    ),
                )

                every { memberJpaRepository.existsById(memberId) } returns true
                every { emotionJpaRepository.findEmotionsByMemberId(memberId) } returns emotions

                val result = service.execute(memberId)

                result.totalRecords shouldBe 3
                result.averageConfidence shouldBe 83
                result.streak shouldBe 3
                result.emotions.size shouldBe 3
                result.emotions[0].id shouldBe 1L
                result.emotions[0].emotion shouldBe "행복"
                result.emotions[0].confidence shouldBe 85
            }
        }
    }

    Given("존재하지 않는 멤버 ID가 주어졌을 때") {
        When("감정 목록을 조회하면") {
            Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val memberJpaRepository = mockk<MemberJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository, memberJpaRepository)

                val memberId = 999L
                every { memberJpaRepository.existsById(memberId) } returns false

                val exception = shouldThrow<GlobalException> {
                    service.execute(memberId)
                }
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }
    }

    Given("감정 기록이 없는 멤버 ID가 주어졌을 때") {
        When("감정 목록을 조회하면") {
            Then("빈 목록과 기본값을 반환한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val memberJpaRepository = mockk<MemberJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository, memberJpaRepository)

                val memberId = 1L
                every { memberJpaRepository.existsById(memberId) } returns true
                every { emotionJpaRepository.findEmotionsByMemberId(memberId) } returns emptyList()

                val result = service.execute(memberId)

                result.totalRecords shouldBe 0
                result.averageConfidence shouldBe 0
                result.streak shouldBe 0
                result.emotions.size shouldBe 0
            }
        }
    }

    Given("연속되지 않은 날짜의 감정 기록이 있을 때") {
        When("감정 목록을 조회하면") {
            Then("올바른 streak를 계산한다") {
                val emotionJpaRepository = mockk<EmotionJpaRepository>()
                val memberJpaRepository = mockk<MemberJpaRepository>()
                val service = FindEmotionsByMemberIdService(emotionJpaRepository, memberJpaRepository)

                val memberId = 1L
                val emotions = listOf(
                    EmotionProjection(
                        id = 1L,
                        date = LocalDate.now(),
                        emotion = "행복",
                        confidence = 85L,
                    ),
                    EmotionProjection(
                        id = 2L,
                        date = LocalDate.now().minusDays(1),
                        emotion = "즐거움",
                        confidence = 90L,
                    ),
                    EmotionProjection(
                        id = 3L,
                        date = LocalDate.now().minusDays(5),
                        emotion = "평온",
                        confidence = 75L,
                    ),
                )

                every { memberJpaRepository.existsById(memberId) } returns true
                every { emotionJpaRepository.findEmotionsByMemberId(memberId) } returns emotions

                val result = service.execute(memberId)

                result.streak shouldBe 2
                result.totalRecords shouldBe 3
            }
        }
    }
})
