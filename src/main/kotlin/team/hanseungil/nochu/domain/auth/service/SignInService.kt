package team.hanseungil.nochu.domain.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

@Service
class SignInService(
    private val memberJpaRepository: MemberJpaRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional(readOnly = true)
    fun execute(nickname: String, password: String): Long {
        val member = memberJpaRepository.findByNickname(nickname)
            ?: throw GlobalException(ErrorCode.MEMBER_NOT_FOUND)
        
        if (!passwordEncoder.matches(password, member.password)) {
            throw GlobalException(ErrorCode.MEMBER_NOT_FOUND)
        }
        
        return member.id
    }
}