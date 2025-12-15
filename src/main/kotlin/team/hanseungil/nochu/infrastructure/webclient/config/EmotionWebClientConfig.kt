package team.hanseungil.nochu.infrastructure.webclient.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value

@Configuration
class EmotionWebClientConfig {

    @Value("\${external.emotion.url}")
    private lateinit var emotionBaseUrl: String

    @Bean
    fun emotionWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(emotionBaseUrl)
            .build()
}