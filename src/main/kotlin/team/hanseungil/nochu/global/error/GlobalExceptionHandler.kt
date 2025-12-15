package team.hanseungil.nochu.global.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(GlobalException::class)
    fun handleGlobalException(ex: GlobalException): ResponseEntity<ErrorResponse> {
        logger.warn("GlobalException: {}", ex.message)
        return ResponseEntity
            .status(ex.errorCode.status)
            .body(ErrorResponse(
                status = ex.errorCode.status,
                message = ex.message ?: ex.errorCode.message,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Validation Failed: {}", ex.message)
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage 
            ?: "유효성 검사에 실패했습니다"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = message,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("HttpMessageNotReadable: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "요청 본문을 읽을 수 없습니다",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        logger.warn("HTTP Method Not Supported: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ErrorResponse(
                status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                message = "지원하지 않는 HTTP 메서드입니다: ${ex.method}",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn("Type Mismatch: {}", ex.message)
        val message = "잘못된 요청 파라미터입니다: ${ex.name}"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = message,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected Exception: ", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "서버 내부 오류가 발생했습니다",
                timestamp = LocalDateTime.now()
            ))
    }

    data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: LocalDateTime
    )
}
