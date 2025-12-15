package team.hanseungil.nochu.domain.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.hanseungil.nochu.domain.member.entity.Member

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findByNickname(nickname: String): Member?
    fun existsByNickname(nickname: String): Boolean
}