package team.hanseungil.nochu.domain.emotion.presentation.dto.response

import java.time.LocalDate

data class EmotionListResponse(
    val totalRecords: Int,
    val averageConfidence: Int,
    val streak: Int,
    val emotions: List<Emotions>,
) {
    data class Emotions(
        val id: Long,
        val date: LocalDate,
        val emotion: String,
        val confidence: Int
    )
}
