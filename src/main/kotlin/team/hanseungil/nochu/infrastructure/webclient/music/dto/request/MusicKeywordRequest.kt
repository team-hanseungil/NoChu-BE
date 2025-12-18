package team.hanseungil.nochu.infrastructure.webclient.music.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class MusicKeywordRequest(
    val emotions: Emotions,
    val emotion: String,
) {
    data class Emotions(
        @JsonProperty("행복")
        val happiness: Double,

        @JsonProperty("당황")
        val embarrassment: Double,

        @JsonProperty("분노")
        val anger: Double,

        @JsonProperty("불안")
        val anxiety: Double,

        @JsonProperty("상처")
        val hurt: Double,

        @JsonProperty("슬픔")
        val sadness: Double,
    )
}