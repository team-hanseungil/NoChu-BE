package team.hanseungil.nochu.infrastructure.webclient.emotion.client

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.AnalyzeEmotionResponse

@Component
class EmotionWebClient(
    private val webClient: WebClient,
) {
    suspend fun analyzeEmotion(image: MultipartFile): AnalyzeEmotionResponse {
        val resource = object : ByteArrayResource(image.bytes) {
            override fun getFilename(): String = image.originalFilename ?: "image"
        }

        return webClient.post()
            .uri("/api/ai/emotions")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(
                org.springframework.util.LinkedMultiValueMap<String, Any>().apply {
                    add("image", resource)
                }
            )
            .retrieve()
            .bodyToMono(AnalyzeEmotionResponse::class.java)
            .awaitSingle()
    }
}