package team.hanseungil.nochu.domain.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import team.hanseungil.nochu.domain.member.entity.Member
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository

class SignUpServiceTest : BehaviorSpec({
    val memberJpaRepository = mockk<MemberJpaRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val signUpService = SignUpService(memberJpaRepository, passwordEncoder)

    Given("회원가입 요청이 주어졌을 때") {
        val nickname = "testuser"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val savedMember = Member(id = 1L, nickname = nickname, password = encodedPassword)

        every { passwordEncoder.encode(password) } returns encodedPassword
        every { memberJpaRepository.save(any()) } returns savedMember

        When("회원가입을 실행하면") {
            val result = signUpService.execute(nickname, password)

            Then("memberId를 반환한다") {
                result shouldBe 1L
            }

            Then("비밀번호가 암호화되어 저장된다") {
                verify(exactly = 1) { passwordEncoder.encode(password) }
                verify(exactly = 1) { memberJpaRepository.save(match { it.password == encodedPassword }) }
            }
        }
    }
})
