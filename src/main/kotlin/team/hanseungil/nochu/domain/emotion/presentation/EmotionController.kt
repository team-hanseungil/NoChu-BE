package team.hanseungil.nochu.domain.emotion.presentation

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import team.hanseungil.nochu.domain.emotion.presentation.dto.request.AnalyzeEmotionRequest
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.AnalyzeEmotionResponse
import team.hanseungil.nochu.domain.emotion.service.AnalyzeEmotionService

@RestController
@RequestMapping("/api/emotions")
class EmotionController(
    private val analyzeEmotionService: AnalyzeEmotionService,
) {

    @PostMapping("/{memberId}")
    suspend fun analyzeEmotion(
        @PathVariable("memberId") memberId: Long,
        @Valid @RequestBody request: AnalyzeEmotionRequest
    ): ResponseEntity<AnalyzeEmotionResponse> {
        return ResponseEntity.ok().body(
            analyzeEmotionService.execute(memberId, request.image)
        )
    }
}