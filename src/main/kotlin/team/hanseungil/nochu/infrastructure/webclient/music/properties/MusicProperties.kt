package team.hanseungil.nochu.infrastructure.webclient.music.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "external.music")
data class MusicProperties(
    val url: String,
)
