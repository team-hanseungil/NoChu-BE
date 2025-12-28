package team.hanseungil.nochu.domain.emotion.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.hanseungil.nochu.domain.emotion.presentation.dto.response.FindEmotionsByMemberIdResponse
import team.hanseungil.nochu.domain.emotion.repository.EmotionJpaRepository
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class FindEmotionsByMemberIdService(
    private val emotionJpaRepository: EmotionJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(memberId: Long): FindEmotionsByMemberIdResponse {
        if (!memberJpaRepository.existsById(memberId)) {
            throw GlobalException(ErrorCode.MEMBER_NOT_FOUND)
        }

        val emotions = emotionJpaRepository.findEmotionsByMemberId(memberId)

        val confidences = emotions.mapNotNull { it.confidence }
        val dates = emotions.mapNotNull { it.date }

        val totalRecords = emotions.size
        val averageConfidence = if (confidences.isNotEmpty()) {
            confidences.average().toInt()
        } else {
            0
        }

        val streak = calculateStreak(dates)

        val emotionDtos = emotions.mapNotNull { e ->
            val date = e.date ?: return@mapNotNull null
            FindEmotionsByMemberIdResponse.EmotionDto(
                id = e.id,
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                emotion = e.emotion,
                confidence = e.confidence ?: 0
            )
        }

        return FindEmotionsByMemberIdResponse(
            totalRecords = totalRecords,
            averageConfidence = averageConfidence,
            streak = streak,
            emotions = emotionDtos
        )
    }

    private fun calculateStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.distinct().sortedDescending()
        var streak = 0
        var expectedDate = LocalDate.now()

        for (date in sortedDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (streak == 0 && date.isBefore(expectedDate)) {
                continue
            } else {
                break
            }
        }

        return streak
    }
}