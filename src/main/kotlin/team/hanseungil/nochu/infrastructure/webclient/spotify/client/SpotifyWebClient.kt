package team.hanseungil.nochu.infrastructure.webclient.spotify.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import team.hanseungil.nochu.infrastructure.webclient.spotify.properties.SpotifyProperties
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifyAuthResponse
import team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response.SpotifySearchResponse
import java.util.Base64
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

@Component
class SpotifyWebClient(
    private val webClientBuilder: WebClient.Builder,
    private val spotifyProperties: SpotifyProperties
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SpotifyWebClient::class.java)
    }

    @Volatile private var accessToken: String? = null
    @Volatile private var accessTokenExpiresAtEpochSec: Long? = null
    private val tokenMutex = Mutex()

    suspend fun searchTrackByKeywords(keywords: String, offset: Int = 0): SpotifySearchResponse {
        val normalized = keywords
            .trim()
            .replace(Regex("\\s+"), " ")

        logger.info(
            "Spotify API Request - keywords: '{}', offset: {}, limit: {}",
            normalized,
            offset,
            spotifyProperties.limit
        )

        return try {
            val token = getAccessToken(forceRefresh = false)
            doSearch(token = token, normalized = normalized, offset = offset)
        } catch (e: WebClientResponseException.Unauthorized) {
            logger.warn("Spotify API 401 Unauthorized. Refreshing token and retrying once. offset={}", offset, e)
            clearToken()
            val token = getAccessToken(forceRefresh = true)
            doSearch(token = token, normalized = normalized, offset = offset)
        } catch (e: WebClientResponseException.BadRequest) {
            logger.error("Spotify API 400 BadRequest", e)
            throw GlobalException(ErrorCode.EXTERNAL_API_BAD_REQUEST)
        } catch (e: WebClientResponseException) {
            logger.error("Spotify API WebClientResponseException status={}", e.statusCode, e)
            throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
        } catch (e: TimeoutException) {
            logger.error("Spotify API Timeout", e)
            throw GlobalException(ErrorCode.EXTERNAL_API_TIMEOUT)
        } catch (e: Exception) {
            logger.error("Spotify API Unknown Error", e)
            throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    private suspend fun doSearch(token: String, normalized: String, offset: Int): SpotifySearchResponse {
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
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { resp ->
                resp.bodyToMono(String::class.java).defaultIfEmpty("").flatMap { body ->
                    val status = resp.statusCode()
                    val retryAfter = resp.headers().asHttpHeaders().getFirst("Retry-After")

                    logger.error(
                        "Spotify API ERROR - status={}, retryAfter={}, body={}",
                        status,
                        retryAfter,
                        body
                    )

                    reactor.core.publisher.Mono.error(
                        GlobalException(ErrorCode.EXTERNAL_API_ERROR)
                    )
                }
            }
            .awaitBody<SpotifySearchResponse>()

        logger.info(
            "Spotify API Response - total tracks: {}, first track: {}",
            response.tracks.items.size,
            response.tracks.items.firstOrNull()?.let { "${'$'}{it.name} (${ '$' }{it.album.releaseDate})" } ?: "N/A"
        )
        logger.debug("Spotify API Response - {}", response)
        return response
    }

    private fun clearToken() {
        accessToken = null
        accessTokenExpiresAtEpochSec = null
    }

    private fun isTokenValid(nowEpochSec: Long): Boolean {
        val token = accessToken
        val exp = accessTokenExpiresAtEpochSec
        if (token.isNullOrBlank() || exp == null) return false
        return nowEpochSec < (exp - 60)
    }

    private suspend fun getAccessToken(forceRefresh: Boolean): String {
        val now = Instant.now().epochSecond

        if (!forceRefresh && isTokenValid(now)) {
            return accessToken!!
        }

        return tokenMutex.withLock {
            val nowLocked = Instant.now().epochSecond
            if (!forceRefresh && isTokenValid(nowLocked)) {
                return@withLock accessToken!!
            }

            val credentials = Base64.getEncoder()
                .encodeToString("${'$'}{spotifyProperties.clientId}:${'$'}{spotifyProperties.clientSecret}".toByteArray())

            try {
                val response = webClientBuilder
                    .baseUrl("https://accounts.spotify.com")
                    .build()
                    .post()
                    .uri("/api/token")
                    .header("Authorization", "Basic ${'$'}credentials")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("grant_type=client_credentials")
                    .retrieve()
                    .awaitBody<SpotifyAuthResponse>()

                accessToken = response.accessToken
                accessTokenExpiresAtEpochSec = Instant.now().epochSecond + (response.expiresIn ?: 3600)
                return@withLock response.accessToken
            } catch (e: WebClientResponseException.BadRequest) {
                logger.error("Spotify Auth 400 BadRequest", e)
                throw GlobalException(ErrorCode.EXTERNAL_API_BAD_REQUEST)
            } catch (e: WebClientResponseException) {
                logger.error("Spotify Auth Error status={}", e.statusCode, e)
                throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
            } catch (e: TimeoutException) {
                logger.error("Spotify Auth Timeout", e)
                throw GlobalException(ErrorCode.EXTERNAL_API_TIMEOUT)
            } catch (e: Exception) {
                logger.error("Spotify Auth Unknown Error", e)
                throw GlobalException(ErrorCode.EXTERNAL_API_ERROR)
            }
        }
    }
}
