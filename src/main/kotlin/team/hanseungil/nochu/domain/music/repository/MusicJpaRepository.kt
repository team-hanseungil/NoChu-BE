package team.hanseungil.nochu.domain.music.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.hanseungil.nochu.domain.music.entity.Music

interface MusicJpaRepository : JpaRepository<Music, Long> {
    
    @Query("SELECT COUNT(m) FROM Music m WHERE m.playlist.id = :playlistId")
    fun countByPlaylistId(playlistId: Long): Long

    fun findAllByPlaylistIdOrderBySortOrderAsc(playlistId: Long): List<Music>
}