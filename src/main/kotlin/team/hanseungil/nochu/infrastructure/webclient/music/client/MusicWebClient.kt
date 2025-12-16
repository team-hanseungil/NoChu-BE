package team.hanseungil.nochu.infrastructure.webclient.music.client

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import team.hanseungil.nochu.infrastructure.webclient.music.dto.request.MusicKeywordRequest
import team.hanseungil.nochu.infrastructure.webclient.music.dto.response.MusicKeywordResponse
import team.hanseungil.nochu.infrastructure.webclient.music.properties.MusicProperties

@Component
class MusicWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val musicProperties: MusicProperties,
) {
    suspend fun extractKeywords(
        request: MusicKeywordRequest
    ): MusicKeywordResponse {
        return webClientBuilder
            .baseUrl(musicProperties.url)
            .build()
            .post()
            .uri("/api/ai/keywords")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(MusicKeywordResponse::class.java)
            .awaitSingle()
    }
}