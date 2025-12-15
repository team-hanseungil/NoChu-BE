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
        if (!memberJpaRepository.existsById(memberId)) {
            throw GlobalException(ErrorCode.MEMBER_NOT_FOUND)
        }

        val analysisDeferred = async {
            emotionWebClient.analyzeEmotion(image)
        }

        val uploadDeferred = async(Dispatchers.IO) {
            s3UploadService.execute(image)
        }

        val emotionAnalysisResponse = analysisDeferred.await()
        val imageUrl = uploadDeferred.await()

        val emotionsMap = mapOf(
            "happy" to emotionAnalysisResponse.emotions.happy,
            "surprise" to emotionAnalysisResponse.emotions.surprise,
            "anger" to emotionAnalysisResponse.emotions.anger,
            "anxiety" to emotionAnalysisResponse.emotions.anxiety,
            "hurt" to emotionAnalysisResponse.emotions.hurt,
            "sad" to emotionAnalysisResponse.emotions.sad,
        )

        transactionTemplate.executeWithoutResult {
            val member = memberJpaRepository.findById(memberId)
                .orElseThrow { GlobalException(ErrorCode.MEMBER_NOT_FOUND) }

            val emotion = Emotion(
                emotions = emotionsMap,
                imageUrl = imageUrl,
                memberId = member.id,
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