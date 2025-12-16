package team.hanseungil.nochu.domain.music.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import team.hanseungil.nochu.domain.playlist.entity.Playlist
import java.time.LocalDateTime

@Entity
@Table(name = "tb_music")
class Music(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
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

    @Column(name = "spotify_id", nullable = false)
    val spotifyId: String,

    @Column(name = "spotify_url", nullable = false)
    val spotifyUrl: String,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    val playlist: Playlist,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String?
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
