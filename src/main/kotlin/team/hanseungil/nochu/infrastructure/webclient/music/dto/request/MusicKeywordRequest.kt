package team.hanseungil.nochu.infrastructure.webclient.music.dto.request

data class MusicKeywordRequest(
    val happy: Double,
    val surprise: Double,
    val anger: Double,
    val anxiety: Double,
    val hurt: Double,
    val sad: Double,
)
