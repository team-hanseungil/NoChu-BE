package team.hanseungil.nochu.domain.auth.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.hanseungil.nochu.domain.auth.presentation.dto.response.AuthResponse
import team.hanseungil.nochu.domain.auth.presentation.dto.request.SignInRequest
import team.hanseungil.nochu.domain.auth.presentation.dto.request.SignUpRequest
import team.hanseungil.nochu.domain.auth.service.SignInService
import team.hanseungil.nochu.domain.auth.service.SignUpService

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val signUpService: SignUpService,
    private val signInService: SignInService,
) {
    
    @Operation(summary = "회원가입", description = "닉네임과 비밀번호로 회원가입을 진행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 닉네임 등)")
        ]
    )
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest
    ): ResponseEntity<AuthResponse> {
        val response = AuthResponse(signUpService.execute(
            nickname = request.nickname,
            password = request.password))
        return ResponseEntity.ok().body(response)
    }
    
    @Operation(summary = "로그인", description = "닉네임과 비밀번호로 로그인을 진행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 닉네임 또는 비밀번호)")
        ]
    )
    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody request: SignInRequest
    ): ResponseEntity<AuthResponse> {
        val response = AuthResponse(signInService.execute(
            nickname = request.nickname,
            password = request.password))
        return ResponseEntity.ok(response)
    }
}