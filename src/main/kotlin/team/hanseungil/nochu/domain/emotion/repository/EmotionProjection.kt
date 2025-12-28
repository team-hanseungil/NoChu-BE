package team.hanseungil.nochu.domain.emotion.repository

import java.time.LocalDate

data class EmotionProjection(
    val id: Long,
    val date: LocalDate,
    val emotion: String,
    val confidence: Long?
)
