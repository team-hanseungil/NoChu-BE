package team.hanseungil.nochu.infrastructure.webclient.emotion

import team.hanseungil.nochu.domain.emotion.presentation.dto.Emotions

data class EmotionResponse(
    val emotions: Emotions,
    val emotion: String,
    val comment: String,
)
