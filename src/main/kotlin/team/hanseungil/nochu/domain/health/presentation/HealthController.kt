package team.hanseungil.nochu.domain.health.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/health")
class HealthController {
    @GetMapping("/check")
    fun check() = "ok"
}