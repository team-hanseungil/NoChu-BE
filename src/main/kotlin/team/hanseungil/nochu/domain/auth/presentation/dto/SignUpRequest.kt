package team.hanseungil.nochu.domain.auth.presentation.dto

import jakarta.validation.constraints.NotBlank

data class SignUpRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    val nickname: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String,
)