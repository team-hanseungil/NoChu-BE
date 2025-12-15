package team.hanseungil.nochu.domain.emotion.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import team.hanseungil.nochu.domain.emotion.mapper.EmotionMapMapper
import java.time.LocalDateTime

@Entity
@Table(name = "tb_emotion")
class Emotion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "emotions", nullable = false, columnDefinition = "json")
    @Convert(converter = EmotionMapMapper::class)
    val emotions: Map<String, Double>,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
