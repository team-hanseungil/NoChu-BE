package team.hanseungil.nochu.infrastructure.webclient.music.dto.request

data class MusicKeywordRequest(
    val emotions: Emotions,
) {
    data class Emotions(
        val happy: Double,
        val surprise: Double,
        val anger: Double,
        val anxiety: Double,
        val hurt: Double,
        val sad: Double,
    )
}