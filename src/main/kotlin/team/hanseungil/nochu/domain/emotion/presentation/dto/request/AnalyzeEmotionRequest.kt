package team.hanseungil.nochu.domain.emotion.presentation.dto.request

import org.springframework.web.multipart.MultipartFile

data class AnalyzeEmotionRequest(
    val image: MultipartFile
)
