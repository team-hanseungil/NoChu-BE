package team.hanseungil.nochu.domain.playlist.presentation.dto.response

data class PlaylistListResponse(
    val playlists: List<PlaylistDto>
) {
    data class PlaylistDto(
        val id: Long,
        val title: String,
        val emotion: String,
        val imageUrl: String?,
        val count: Long,
        val createdAt: String
    )
}
