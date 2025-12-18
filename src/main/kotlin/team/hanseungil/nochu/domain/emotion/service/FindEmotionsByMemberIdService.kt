package team.hanseungil.nochu.domain.emotion.service

import org.springframework.stereotype.Service
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.EmotionListResponse
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class FindEmotionsByMemberIdService(
    private val emotionJpaRepository: EmotionJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
    fun execute(memberId: Long): EmotionListResponse {
        val member = memberJpaRepository.findById(memberId)
            .orElseThrow { GlobalException(ErrorCode.MEMBER_NOT_FOUND) }

        val emotions = emotionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)

        val totalRecords = emotions.size
        val averageConfidence = if (emotions.isNotEmpty()) {
            emotions.map { it.emotions[it.emotion] ?: 0.0 }
                .average()
                .times(100)
                .toInt()
        } else 0

        val streak = calculateStreak(emotions.map { it.createdAt.toLocalDate() })

        val emotionList = emotions.map {
            EmotionListResponse.Emotions(
                id = it.id,
                date = it.createdAt.toLocalDate(),
                emotion = it.emotion,
                confidence = ((it.emotions[it.emotion] ?: 0.0) * 100).toInt()
            )
        }

        return EmotionListResponse(
            totalRecords = totalRecords,
            averageConfidence = averageConfidence,
            streak = streak,
            emotions = emotionList
        )
    }

    private fun calculateStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.distinct().sortedDescending()
        val today = LocalDate.now()

        if (ChronoUnit.DAYS.between(sortedDates.first(), today) > 1) return 0

        var streak = 0
        var expectedDate = today

        for (date in sortedDates) {
            val daysDiff = ChronoUnit.DAYS.between(date, expectedDate)
            when {
                daysDiff == 0L -> {
                    streak++
                    expectedDate = date.minusDays(1)
                }
                daysDiff == 1L && expectedDate == today -> {
                    expectedDate = date.minusDays(1)
                }
                else -> break
            }
        }

        return streak
    }
}