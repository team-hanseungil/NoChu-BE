package team.hanseungil.nochu.domain.auth.presentation

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val signUpService: SignUpService,
    private val signInService: SignInService,
) {
    
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<AuthResponse> {
        val memberId = signUpService.execute(request.nickname, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse(memberId))
    }
    
    @PostMapping("/signin")
    fun signIn(@Valid @RequestBody request: SignInRequest): ResponseEntity<AuthResponse> {
        val memberId = signInService.execute(request.nickname, request.password)
        return ResponseEntity.ok(AuthResponse(memberId))
    }
}