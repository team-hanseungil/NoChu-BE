package team.hanseungil.nochu.domain.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.hanseungil.nochu.domain.member.entity.Member
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

@Service
class SignUpService(
    private val memberJpaRepository: MemberJpaRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun execute(nickname: String, password: String): Long {
        if (memberJpaRepository.existsByNickname(nickname)){
            throw GlobalException(ErrorCode.MEMBER_NICKNAME_ALREADY_EXISTS)
        }

        val encodedPassword = passwordEncoder.encode(password)
        val member = Member(nickname = nickname, password = encodedPassword!!)
        val savedMember = memberJpaRepository.save(member)
        return savedMember.id
    }
}