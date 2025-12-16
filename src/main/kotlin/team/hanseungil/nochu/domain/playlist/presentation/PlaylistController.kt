package team.hanseungil.nochu.domain.playlist.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistListResponse
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistResponse
import team.hanseungil.nochu.domain.playlist.service.PlaylistDetailService
import team.hanseungil.nochu.domain.playlist.service.PlaylistListService

@Tag(name = "Playlist", description = "플레이리스트 API")
@RestController
@RequestMapping("/api/playlists")
class PlaylistController(
    private val playlistListService: PlaylistListService,
    private val playlistDetailService: PlaylistDetailService
) {

    @Operation(summary = "회원의 플레이리스트 목록 조회", description = "특정 회원의 모든 플레이리스트를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PlaylistListResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
        ]
    )
    @GetMapping("/member/{memberId}")
    suspend fun getPlaylists(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable("memberId") memberId: Long
    ): ResponseEntity<PlaylistListResponse> {
        val response = playlistListService.execute(memberId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "플레이리스트 상세 조회", description = "플레이리스트의 상세 정보와 포함된 음악 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PlaylistResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없음")
        ]
    )
    @GetMapping("/{playlistId}")
    suspend fun getPlaylist(
        @Parameter(description = "플레이리스트 ID", required = true)
        @PathVariable("playlistId") playlistId: Long
    ): ResponseEntity<PlaylistResponse> {
        val response = playlistDetailService.execute(playlistId)
        return ResponseEntity.ok(response)
    }
}
