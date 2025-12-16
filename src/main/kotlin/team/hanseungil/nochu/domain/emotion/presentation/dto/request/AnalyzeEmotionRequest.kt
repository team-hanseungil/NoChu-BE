package team.hanseungil.nochu.domain.emotion.presentation.dto.request

import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class AnalyzeEmotionRequest(
    @field:NotNull(message = "이미지는 필수입니다")
    val image: MultipartFile
)
