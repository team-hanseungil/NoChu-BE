package team.hanseungil.nochu.domain.playlist.presentation.dto.response

data class PlaylistResponse(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val tracks: List<Track>
) {
    data class Track(
        val artists: List<String>,
        val title: String,
        val imageUrl: String?,
        val spotifyUrl: String?,
        val duration: String
    )
}