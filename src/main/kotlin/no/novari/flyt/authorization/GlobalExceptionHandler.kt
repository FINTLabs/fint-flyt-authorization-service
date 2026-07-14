package no.novari.flyt.authorization

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(exception: ResponseStatusException): ProblemDetail {
        logger.warn("Response status exception with status={}", exception.statusCode, exception)
        return exception.reason
            ?.let { ProblemDetail.forStatusAndDetail(exception.statusCode, it) }
            ?: ProblemDetail.forStatus(exception.statusCode)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(exception: HttpMessageNotReadableException): ProblemDetail {
        logger.warn("Malformed request body", exception)
        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Malformed request body",
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(exception: MethodArgumentTypeMismatchException): ProblemDetail {
        logger.warn("Request parameter type mismatch", exception)
        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Invalid value for request parameter '${exception.name}'",
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ProblemDetail {
        logger.warn("Invalid or incomplete authentication token", exception)
        return createProblemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Invalid or incomplete authentication token",
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(exception: Exception): ProblemDetail {
        logger.error("Unhandled exception", exception)
        return createProblemDetail(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            title = "Internal Server Error",
            detail = "An unexpected error occurred",
        )
    }

    private fun createProblemDetail(
        status: HttpStatus,
        title: String,
        detail: String,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            this.title = title
        }
}
