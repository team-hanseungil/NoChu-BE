package team.hanseungil.nochu.infrastructure.webclient.music.client

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.webclient.music.dto.request.MusicKeywordRequest
import team.hanseungil.nochu.infrastructure.webclient.music.dto.response.MusicKeywordResponse
import team.hanseungil.nochu.infrastructure.webclient.music.properties.MusicProperties
import java.util.concurrent.TimeoutException

@Component
class MusicWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val musicProperties: MusicProperties,
) {
    suspend fun extractKeywords(
        request: MusicKeywordRequest
    ): MusicKeywordResponse {
        return try {
            webClientBuilder
                .baseUrl(musicProperties.url)
                .build()
                .post()
                .uri("/api/ai/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MusicKeywordResponse::class.java)
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