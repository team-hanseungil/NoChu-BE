package team.hanseungil.nochu.domain.music.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import team.hanseungil.nochu.domain.emotion.entity.Emotion
import java.time.LocalDateTime

@Entity
@Table(name = "tb_music")
class Music(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "artist", nullable = false)
    val artist: String,

    @Column(name = "album")
    val album: String? = null,

    @Column(name = "genre")
    val genre: String? = null,

    @Column(name = "duration_ms")
    val durationMs: Int? = null,

    @Column(name = "spotify_id")
    val spotifyId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    val emotion: Emotion,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
