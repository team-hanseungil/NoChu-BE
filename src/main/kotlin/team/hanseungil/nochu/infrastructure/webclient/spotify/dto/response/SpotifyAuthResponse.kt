package team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifyAuthResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("expires_in")
    val expiresIn: Int
)