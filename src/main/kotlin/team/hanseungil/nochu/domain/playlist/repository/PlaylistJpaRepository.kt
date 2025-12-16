package team.hanseungil.nochu.domain.playlist.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import team.hanseungil.nochu.domain.playlist.entity.Playlist

interface PlaylistJpaRepository : JpaRepository<Playlist, Long> {

    @Query("SELECT p FROM Playlist p JOIN FETCH p.emotion e WHERE e.memberId = :memberId ORDER BY e.createdAt DESC")
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<Playlist>
}