package team.hanseungil.nochu.infrastructure.webclient.spotify.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "external.spotify")
data class SpotifyProperties(
    val url: String,
    val clientId: String,
    val clientSecret: String,
    val limit: Int,
)