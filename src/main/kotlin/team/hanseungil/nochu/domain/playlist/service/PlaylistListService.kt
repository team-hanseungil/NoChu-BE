package team.hanseungil.nochu.domain.playlist.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistListResponse
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import java.time.format.DateTimeFormatter

@Service
class PlaylistListService(
    private val playlistJpaRepository: PlaylistJpaRepository,
    private val musicJpaRepository: MusicJpaRepository
) {
    @Transactional(readOnly = true)
    suspend fun execute(memberId: Long): PlaylistListResponse {
        val playlists = playlistJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
        
        val playlistDtos = playlists.map { playlist ->
            val musicCount = musicJpaRepository.countByPlaylistId(playlist.id)
            
            PlaylistListResponse.PlaylistDto(
                id = playlist.id,
                title = playlist.title,
                emotion = playlist.emotion.emotion,
                imageUrl = playlist.imageUrl,
                count = musicCount,
                createdAt = playlist.emotion.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        }
        
        return PlaylistListResponse(playlists = playlistDtos)
    }
}
