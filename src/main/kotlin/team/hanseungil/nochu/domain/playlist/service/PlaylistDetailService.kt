package team.hanseungil.nochu.domain.playlist.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.hanseungil.nochu.domain.music.repository.MusicJpaRepository
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistResponse
import team.hanseungil.nochu.domain.playlist.repository.PlaylistJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

@Service
class PlaylistDetailService(
    private val playlistJpaRepository: PlaylistJpaRepository,
    private val musicJpaRepository: MusicJpaRepository
) {

    @Transactional(readOnly = true)
    suspend fun execute(playlistId: Long): PlaylistResponse {
        val playlist = playlistJpaRepository.findById(playlistId)
            .orElseThrow { GlobalException(ErrorCode.PLAYLIST_NOT_FOUND) }

        val musics = musicJpaRepository.findAllByPlaylistIdOrderBySortOrderAsc(playlist.id!!)

        val tracks = musics.map { music ->
            PlaylistResponse.Track(
                title = music.title,
                artists = listOf(music.artist),
                duration = formatDuration(music.durationMs ?: 0),
                imageUrl = music.imageUrl,
                spotifyUrl = music.spotifyUrl
            )
        }

        return PlaylistResponse(
            id = playlist.id!!,
            title = playlist.title,
            imageUrl = playlist.imageUrl,
            tracks = tracks
        )
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
