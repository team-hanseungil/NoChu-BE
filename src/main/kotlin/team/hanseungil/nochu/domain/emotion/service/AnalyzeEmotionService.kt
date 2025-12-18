package team.hanseungil.nochu.domain.emotion.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.AnalyzeEmotionResponse
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.s3.service.S3UploadService
import team.hanseungil.nochu.infrastructure.webclient.emotion.client.EmotionWebClient

@Service
class AnalyzeEmotionService(
    private val emotionWebClient: EmotionWebClient,
    private val memberJpaRepository: MemberJpaRepository,
    private val emotionJpaRepository: EmotionJpaRepository,
    private val s3UploadService: S3UploadService,
    private val transactionTemplate: TransactionTemplate,
) {
    suspend fun execute(memberId: Long, image: MultipartFile): AnalyzeEmotionResponse = coroutineScope {
        val analysisDeferred = async {
            emotionWebClient.analyzeEmotion(image)
        }

        val uploadDeferred = async(Dispatchers.IO) {
            s3UploadService.execute(image)
        }

        val emotionAnalysisResponse = analysisDeferred.await()
        val imageUrl = uploadDeferred.await()

        val emotionsMap = mapOf(
            "행복" to emotionAnalysisResponse.emotions.happy,
            "당황" to emotionAnalysisResponse.emotions.surprise,
            "분노" to emotionAnalysisResponse.emotions.anger,
            "불안" to emotionAnalysisResponse.emotions.anxiety,
            "상처" to emotionAnalysisResponse.emotions.hurt,
            "슬픔" to emotionAnalysisResponse.emotions.sad,
        )

        transactionTemplate.executeWithoutResult {
            val member = memberJpaRepository.findById(memberId)
                .orElseThrow { GlobalException(ErrorCode.MEMBER_NOT_FOUND) }

            val emotion = Emotion(
                emotions = emotionsMap,
                imageUrl = imageUrl,
                memberId = member.id,
                emotion = emotionAnalysisResponse.emotion,
            )

            emotionJpaRepository.save(emotion)
        }

        AnalyzeEmotionResponse(
            imageUrl = imageUrl,
            emotions = emotionAnalysisResponse.emotions,
            emotion = emotionAnalysisResponse.emotion,
            comment = emotionAnalysisResponse.comment,
        )
    }
}