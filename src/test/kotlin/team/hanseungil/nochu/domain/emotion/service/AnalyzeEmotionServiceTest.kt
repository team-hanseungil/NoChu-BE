package team.hanseungil.nochu.domain.emotion.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.AnalyzeEmotionResponse
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.member.entity.Member
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.s3.service.S3UploadService
import team.hanseungil.nochu.infrastructure.webclient.emotion.client.EmotionWebClient
import java.util.*

class AnalyzeEmotionServiceTest : BehaviorSpec({
    Given("유효한 멤버와 이미지가 주어졌을 때") {
        When("감정 분석을 실행하면") {
            Then("감정 분석 결과를 반환한다") {
                runTest {
                    val emotionWebClient = mockk<EmotionWebClient>()
                    val memberJpaRepository = mockk<MemberJpaRepository>()
                    val emotionJpaRepository = mockk<EmotionJpaRepository>()
                    val s3UploadService = mockk<S3UploadService>()
                    val transactionTemplate = mockk<TransactionTemplate>()
                    val service = AnalyzeEmotionService(
                        emotionWebClient,
                        memberJpaRepository,
                        emotionJpaRepository,
                        s3UploadService,
                        transactionTemplate
                    )

                    val memberId = 1L
                    val image = mockk<MultipartFile>()
                    val response = AnalyzeEmotionResponse(
                        emotions = AnalyzeEmotionResponse.Emotions(0.8, 0.1, 0.02, 0.03, 0.02, 0.03),
                        emotion = "행복",
                        comment = "테스트",
                        imageUrl = "https://s3.com/test.jpg"
                    )

                    val member = Member(id = memberId, nickname = "testUser", password = "password")
                    
                    coEvery { emotionWebClient.analyzeEmotion(image) } returns response
                    every { s3UploadService.execute(image) } returns "https://s3.com/test.jpg"
                    every { memberJpaRepository.findById(memberId) } returns Optional.of(member)
                    every { emotionJpaRepository.save(any<Emotion>()) } returns mockk(relaxed = true)
                    
                    val slot = slot<java.util.function.Consumer<org.springframework.transaction.TransactionStatus>>()
                    every { transactionTemplate.executeWithoutResult(capture(slot)) } answers {
                        slot.captured.accept(mockk(relaxed = true))
                    }

                    val result = service.execute(memberId, image)

                    result.emotion shouldBe "행복"
                    verify(exactly = 1) { memberJpaRepository.findById(memberId) }
                }
            }
        }
    }

    Given("존재하지 않는 멤버 ID가 주어졌을 때") {
        When("감정 분석을 실행하면") {
            Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                runTest {
                    val emotionWebClient = mockk<EmotionWebClient>()
                    val memberJpaRepository = mockk<MemberJpaRepository>()
                    val emotionJpaRepository = mockk<EmotionJpaRepository>()
                    val s3UploadService = mockk<S3UploadService>()
                    val transactionTemplate = mockk<TransactionTemplate>()
                    val service = AnalyzeEmotionService(
                        emotionWebClient,
                        memberJpaRepository,
                        emotionJpaRepository,
                        s3UploadService,
                        transactionTemplate
                    )

                    val image = mockk<MultipartFile>()
                    val response = AnalyzeEmotionResponse(
                        emotions = AnalyzeEmotionResponse.Emotions(0.8, 0.1, 0.02, 0.03, 0.02, 0.03),
                        emotion = "행복",
                        comment = "테스트",
                        imageUrl = "https://s3.com/test.jpg"
                    )
                    
                    coEvery { emotionWebClient.analyzeEmotion(image) } returns response
                    every { s3UploadService.execute(image) } returns "https://s3.com/test.jpg"
                    every { memberJpaRepository.findById(999L) } returns Optional.empty()
                    
                    val slot = slot<java.util.function.Consumer<org.springframework.transaction.TransactionStatus>>()
                    every { transactionTemplate.executeWithoutResult(capture(slot)) } answers {
                        slot.captured.accept(mockk(relaxed = true))
                    }

                    val exception = shouldThrow<GlobalException> {
                        service.execute(999L, image)
                    }
                    exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
    }
})
