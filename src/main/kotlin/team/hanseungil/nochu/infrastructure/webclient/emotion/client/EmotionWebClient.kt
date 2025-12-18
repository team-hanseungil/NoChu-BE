package team.hanseungil.nochu.infrastructure.webclient.emotion.client

import AnalyzeEmotionResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import team.hanseungil.nochu.infrastructure.webclient.emotion.properties.EmotionProperties

@Component
class EmotionWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val emotionProperties: EmotionProperties,
) {
    suspend fun analyzeEmotion(image: MultipartFile): AnalyzeEmotionResponse {
        val resource = object : ByteArrayResource(image.bytes) {
            override fun getFilename(): String = image.originalFilename ?: "image"
        }

        return webClientBuilder
            .baseUrl(emotionProperties.url)
            .build()
            .post()
            .uri("/api/ai/emotions")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(
                LinkedMultiValueMap<String, Any>().apply {
                    add("image", resource)
                }
            )
            .retrieve()
            .bodyToMono(AnalyzeEmotionResponse::class.java)
            .awaitSingle()
    }
}