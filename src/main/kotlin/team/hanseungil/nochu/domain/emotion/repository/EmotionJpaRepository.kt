package team.hanseungil.nochu.domain.emotion.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.hanseungil.nochu.domain.emotion.entity.Emotion

interface EmotionJpaRepository : JpaRepository<Emotion, Long> {
}