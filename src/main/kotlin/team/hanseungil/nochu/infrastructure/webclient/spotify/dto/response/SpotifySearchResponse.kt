package team.hanseungil.nochu.infrastructure.webclient.spotify.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class SpotifySearchResponse(
    val tracks: Tracks
) {
    data class Tracks(
        val items: List<Track>
    )

    data class Track(
        val id: String,
        val name: String,
        val artists: List<Artist>,
        val album: Album,
        @JsonProperty("duration_ms")
        val durationMs: Int?,
        @JsonProperty("preview_url")
        val previewUrl: String? = null,
        val popularity: Int? = null,
        @JsonProperty("external_urls")
        val externalUrls: ExternalUrls?,
    )

    data class Artist(
        val name: String,
    )

    data class Album(
        val name: String,
        val images: List<Image>? = null,
        @JsonProperty("release_date")
        val releaseDate: String? = null,
        @JsonProperty("release_date_precision")
        val releaseDatePrecision: String? = null
    )

    data class Image(
        val url: String
    )

    data class ExternalUrls(
        val spotify: String,
    )
}