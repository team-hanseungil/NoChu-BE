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

    @Query(
        value = """
        SELECT
            e.id AS id,
            DATE(e.created_at) AS date,
            e.emotion AS emotion,
            COALESCE(
                CAST(JSON_EXTRACT(e.emotions, CONCAT('$."', e.emotion, '"')) * 100 AS SIGNED),
                0
            ) AS confidence
        FROM tb_emotion e
        WHERE e.member_id = :memberId
        ORDER BY e.created_at DESC
    """,
        nativeQuery = true
    )
    fun findEmotionsByMemberId(@Param("memberId") memberId: Long): List<EmotionProjection>
}