package team.hanseungil.nochu.domain.emotion.repository.projection

import java.time.LocalDateTime

interface EmotionProjection {
    val id: Long
    val emotion: String
    val createdAt: LocalDateTime
    val emotions: Map<String, Double>
}
