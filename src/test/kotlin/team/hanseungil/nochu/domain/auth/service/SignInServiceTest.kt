package team.hanseungil.nochu.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import team.hanseungil.nochu.domain.member.entity.Member
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

class SignInServiceTest : BehaviorSpec({
    val memberJpaRepository = mockk<MemberJpaRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val signInService = SignInService(memberJpaRepository, passwordEncoder)

    Given("올바른 닉네임과 비밀번호가 주어졌을 때") {
        val nickname = "testuser"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val member = Member(id = 1L, nickname = nickname, password = encodedPassword)

        every { memberJpaRepository.findByNickname(nickname) } returns member
        every { passwordEncoder.matches(password, encodedPassword) } returns true

        When("로그인을 실행하면") {
            val result = signInService.execute(nickname, password)

            Then("memberId를 반환한다") {
                result shouldBe 1L
            }
        }
    }

    Given("존재하지 않는 닉네임이 주어졌을 때") {
        val nickname = "nonexistent"
        val password = "password123"

        every { memberJpaRepository.findByNickname(nickname) } returns null

        When("로그인을 실행하면") {
            Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    signInService.execute(nickname, password)
                }
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }
    }

    Given("잘못된 비밀번호가 주어졌을 때") {
        val nickname = "testuser"
        val password = "wrongpassword"
        val encodedPassword = "encodedPassword"
        val member = Member(id = 1L, nickname = nickname, password = encodedPassword)

        every { memberJpaRepository.findByNickname(nickname) } returns member
        every { passwordEncoder.matches(password, encodedPassword) } returns false

        When("로그인을 실행하면") {
            Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    signInService.execute(nickname, password)
                }
                exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            }
        }
    }
})
