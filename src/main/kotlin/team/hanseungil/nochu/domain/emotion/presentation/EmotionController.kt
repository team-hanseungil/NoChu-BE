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
import team.hanseungil.nochu.domain.emotion.service.AnalyzeEmotionService

@Tag(name = "Emotion", description = "감정 분석 API")
@RestController
@RequestMapping("/api/emotions")
class EmotionController(
    private val analyzeEmotionService: AnalyzeEmotionService,
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
}