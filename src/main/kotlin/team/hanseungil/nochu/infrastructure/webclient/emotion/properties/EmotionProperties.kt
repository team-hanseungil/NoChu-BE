package team.hanseungil.nochu.infrastructure.webclient.emotion.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "external.emotion")
data class EmotionProperties(
    val url: String,
)
