package team.hanseungil.nochu.domain.emotion.repository

import java.time.LocalDate

interface EmotionProjection {
    val id: Long
    val date: LocalDate
    val emotion: String
    val confidence: Int
}
