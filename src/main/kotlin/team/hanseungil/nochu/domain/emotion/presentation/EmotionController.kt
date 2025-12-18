package team.hanseungil.nochu.domain.emotion.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.AnalyzeEmotionResponse
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.FindEmotionsByMemberIdResponse
import team.hanseungil.nochu.domain.emotion.service.AnalyzeEmotionService
import team.hanseungil.nochu.domain.emotion.service.FindEmotionsByMemberIdService

@Tag(name = "Emotion", description = "감정 분석 API")
@RestController
@RequestMapping("/api/emotions")
class EmotionController(
    private val analyzeEmotionService: AnalyzeEmotionService,
    private val findEmotionsByMemberIdService: FindEmotionsByMemberIdService,
) {

    @Operation(summary = "감정 분석", description = "이미지를 업로드하여 감정을 분석합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "감정 분석 성공",
                content = [Content(schema = Schema(implementation = AnalyzeEmotionResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
        ]
    )
    @PostMapping("/{memberId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun analyzeEmotion(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable("memberId") memberId: Long,
        @Parameter(description = "감정 분석할 이미지 파일", required = true)
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<AnalyzeEmotionResponse> {
        val response = analyzeEmotionService.execute(
            memberId = memberId,
            image = image
        )
        return ResponseEntity.ok().body(response)
    }

    @Operation(summary = "회원 감정 목록 조회", description = "회원 ID로 감정 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = FindEmotionsByMemberIdResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
        ]
    )
    @GetMapping("/{memberId}")
    fun findEmotionsByMemberId(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable("memberId") memberId: Long
    ): ResponseEntity<FindEmotionsByMemberIdResponse> {
        val response = findEmotionsByMemberIdService.execute(memberId)
        return ResponseEntity.ok().body(response)
    }
}