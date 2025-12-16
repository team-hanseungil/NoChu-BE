package team.hanseungil.nochu.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import team.hanseungil.nochu.domain.member.entity.Member
import team.hanseungil.nochu.domain.member.repository.MemberJpaRepository
import team.hanseungil.nochu.global.error.ErrorCode
import team.hanseungil.nochu.global.error.GlobalException

class SignUpServiceTest : BehaviorSpec({
    Given("유효한 닉네임과 비밀번호가 주어졌을 때") {
        val memberJpaRepository = mockk<MemberJpaRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val signUpService = SignUpService(memberJpaRepository, passwordEncoder)
        
        val nickname = "testuser"
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val savedMember = Member(id = 1L, nickname = nickname, password = encodedPassword)

        every { memberJpaRepository.existsByNickname(nickname) } returns false
        every { passwordEncoder.encode(password) } returns encodedPassword
        every { memberJpaRepository.save(any()) } returns savedMember

        When("회원가입을 실행하면") {
            val result = signUpService.execute(nickname, password)

            Then("memberId를 반환한다") {
                result shouldBe 1L
            }
        }
    }

    Given("이미 존재하는 닉네임이 주어졌을 때") {
        val memberJpaRepository = mockk<MemberJpaRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val signUpService = SignUpService(memberJpaRepository, passwordEncoder)
        
        val nickname = "existingUser"
        val password = "password123"

        every { memberJpaRepository.existsByNickname(nickname) } returns true

        When("회원가입을 실행하면") {
            Then("MEMBER_NICKNAME_ALREADY_EXISTS 예외가 발생한다") {
                val exception = shouldThrow<GlobalException> {
                    signUpService.execute(nickname, password)
                }
                exception.errorCode shouldBe ErrorCode.MEMBER_NICKNAME_ALREADY_EXISTS
            }
        }
    }

    Given("빈 닉네임이 주어졌을 때") {
        val memberJpaRepository = mockk<MemberJpaRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val signUpService = SignUpService(memberJpaRepository, passwordEncoder)
        
        val nickname = ""
        val password = "password123"
        val encodedPassword = "encodedPassword"
        val savedMember = Member(id = 1L, nickname = nickname, password = encodedPassword)

        every { memberJpaRepository.existsByNickname(nickname) } returns false
        every { passwordEncoder.encode(password) } returns encodedPassword
        every { memberJpaRepository.save(any()) } returns savedMember

        When("회원가입을 실행하면") {
            val result = signUpService.execute(nickname, password)

            Then("정상적으로 memberId를 반환한다") {
                result shouldBe 1L
                verify(exactly = 1) { memberJpaRepository.save(any()) }
            }
        }
    }
})
