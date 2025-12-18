package team.hanseungil.nochu.domain.emotion.presentation.dto.response

data class FindEmotionsByMemberIdResponse(
    val totalRecords: Int,
    val averageConfidence: Int,
    val streak: Int,
    val emotions: List<EmotionDto>
) {
    data class EmotionDto(
        val id: Long,
        val date: String,
        val emotion: String,
        val confidence: Int
    )
}
