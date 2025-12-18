import com.fasterxml.jackson.annotation.JsonProperty

data class AnalyzeEmotionResponse(
    val imageUrl: String,
    val emotions: Emotions,
    val emotion: String,
    val comment: String,
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