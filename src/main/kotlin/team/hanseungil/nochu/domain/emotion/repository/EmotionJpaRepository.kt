package team.hanseungil.nochu.domain.emotion.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import java.time.LocalDateTime

interface EmotionJpaRepository : JpaRepository<Emotion, Long> {
    fun findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        memberId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Emotion?

    @Query("""
        SELECT e.id AS id,
               DATE(e.createdAt) AS date,
               e.emotion AS emotion,
               CAST(MAX(JSON_EXTRACT(e.emotions, CONCAT('$."', e.emotion, '"'))) * 100 AS INT) AS confidence
        FROM Emotion e
        WHERE e.memberId = :memberId
        GROUP BY DATE(e.createdAt), e.id, e.emotion
        ORDER BY e.createdAt DESC
    """)
    fun findEmotionsByMemberId(@Param("memberId") memberId: Long): List<EmotionProjection>
}