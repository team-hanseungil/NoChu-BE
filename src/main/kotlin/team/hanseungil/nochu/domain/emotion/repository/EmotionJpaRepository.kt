package team.hanseungil.nochu.domain.emotion.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import java.time.LocalDateTime

interface EmotionJpaRepository : JpaRepository<Emotion, Long> {
    fun findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        memberId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Emotion?
}