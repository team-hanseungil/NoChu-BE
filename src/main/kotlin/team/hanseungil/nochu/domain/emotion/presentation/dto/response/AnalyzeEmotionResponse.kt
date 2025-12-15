package team.hanseungil.nochu.domain.emotion.presentation.dto.response

data class AnalyzeEmotionResponse(
    val imageUrl: String,
    val emotions: Emotions,
    val emotion: String,
    val comment: String,
) {
    data class Emotions(
        val happy: Double,
        val surprise: Double,
        val anger: Double,
        val anxiety: Double,
        val hurt: Double,
        val sad: Double,
    )
}
