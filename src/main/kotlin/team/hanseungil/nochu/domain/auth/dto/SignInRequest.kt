package team.hanseungil.nochu.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class SignInRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    val nickname: String,
    
    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,
)
