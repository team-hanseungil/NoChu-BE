package team.hanseungil.nochu.global.error

import org.springframework.http.HttpStatus

open class GlobalException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message) {
    val statusCode: HttpStatus
        get() = HttpStatus.valueOf(errorCode.status)
}
