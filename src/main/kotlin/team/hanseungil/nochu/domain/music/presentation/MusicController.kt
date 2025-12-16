package team.hanseungil.nochu.domain.music.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.hanseungil.nochu.domain.music.service.MusicRecommendationService
import team.hanseungil.nochu.domain.playlist.presentation.dto.response.PlaylistResponse

@Tag(name = "Music", description = "음악 추천 API")
@RestController
@RequestMapping("/api/music")
class MusicController(
    private val musicRecommendationService: MusicRecommendationService,
) {

    @Operation(
        summary = "감정 기반 음악 추천",
        description = "회원의 오늘 감정 분석 결과를 기반으로 Spotify에서 음악을 추천하고 플레이리스트를 생성합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "음악 추천 성공",
                content = [Content(schema = Schema(implementation = PlaylistResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "회원 또는 감정 데이터를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "음악 추천 실패")
        ]
    )
    @PostMapping("/{memberId}")
    suspend fun getRecommendations(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable("memberId") memberId: Long
    ): ResponseEntity<PlaylistResponse> {
        val response = musicRecommendationService.execute(memberId)
        return ResponseEntity.ok().body(response)
    }
}
