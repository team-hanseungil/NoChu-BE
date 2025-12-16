package team.hanseungil.nochu.domain.playlist.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.hanseungil.nochu.domain.emotion.entity.Emotion

@Entity
@Table(name = "tb_playlist")
class Playlist(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    val emotion: Emotion,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "image_url")
    val imageUrl: String? = null,
)