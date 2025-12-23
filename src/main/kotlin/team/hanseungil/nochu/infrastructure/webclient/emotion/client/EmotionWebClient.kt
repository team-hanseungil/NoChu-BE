package team.hanseungil.nochu.infrastructure.webclient.emotion.client

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.webclient.emotion.EmotionResponse
import team.hanseungil.nochu.infrastructure.webclient.emotion.properties.EmotionProperties
import java.util.concurrent.TimeoutException

@Component
class EmotionWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val emotionProperties: EmotionProperties,
) {
    suspend fun analyzeEmotion(image: MultipartFile): EmotionResponse {
        val resource = object : ByteArrayResource(image.bytes) {
            override fun getFilename(): String = image.originalFilename ?: "image"
        }

        return try {
            webClientBuilder
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
                .bodyToMono(EmotionResponse::class.java)
                .awaitSingle()
        } catch (e: WebClientResponseException.BadRequest) {
            throw GlobalException(ErrorCode.EXTERNAL_API_BAD_REQUEST)
        } catch (e: WebClientResponseException) {
            throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
        } catch (e: TimeoutException) {
            throw GlobalException(ErrorCode.EXTERNAL_API_TIMEOUT)
        } catch (e: Exception) {
            throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }
}