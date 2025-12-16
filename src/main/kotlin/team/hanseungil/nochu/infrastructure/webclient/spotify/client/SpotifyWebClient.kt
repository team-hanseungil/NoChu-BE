package team.hanseungil.nochu.infrastructure.webclient.spotify.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import team.hanseungil.nochu.infrastructure.webclient.spotify.properties.SpotifyProperties
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifyAuthResponse
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifySearchResponse
import java.util.Base64

@Component
class SpotifyWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val spotifyProperties: SpotifyProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SpotifyWebClient::class.java)
    }
    
    private var accessToken: String? = null

    suspend fun searchTrackByKeywords(keywords: String, offset: Int = 0): SpotifySearchResponse {
        val token = getAccessToken()

        val normalized = keywords
            .trim()
            .replace(Regex("\\s+"), " ")

        logger.info("Spotify API Request - keywords: '{}', offset: {}, limit: {}", normalized, offset, spotifyProperties.limit)

        val response = webClientBuilder
            .baseUrl(spotifyProperties.url)
            .build()
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/search")
                    .queryParam("q", normalized)
                    .queryParam("type", "track")
                    .queryParam("limit", spotifyProperties.limit)
                    .queryParam("offset", offset)
                    .queryParam("market", "KR")
                    .build()
            }
            .header("Authorization", "Bearer $token")
            .retrieve()
            .awaitBody<SpotifySearchResponse>()

        logger.info("Spotify API Response - total tracks: {}, first track: {}", 
            response.tracks.items.size,
            response.tracks.items.firstOrNull()?.let { "${it.name} (${it.album.releaseDate})" } ?: "N/A"
        )

        logger.debug("Spotify API Response - {}", response)
        return response
    }

    private suspend fun getAccessToken(): String {
        if (accessToken != null) {
            return accessToken!!
        }

        val credentials = Base64.getEncoder()
            .encodeToString("${spotifyProperties.clientId}:${spotifyProperties.clientSecret}".toByteArray())

        val response = webClientBuilder
            .baseUrl("https://accounts.spotify.com")
            .build()
            .post()
            .uri("/api/token")
            .header("Authorization", "Basic $credentials")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .awaitBody<SpotifyAuthResponse>()

        accessToken = response.accessToken
        return response.accessToken
    }
}
