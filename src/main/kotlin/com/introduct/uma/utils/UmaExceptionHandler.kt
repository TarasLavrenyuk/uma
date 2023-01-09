package com.introduct.uma.utils

import java.time.OffsetDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class UmaExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [ResponseStatusException::class])
    fun handle(exception: ResponseStatusException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(exception.reason ?: GENERIC_ERROR_MESSAGE),
            exception.status
        )
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleUnknown(exception: Throwable): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception was thrown. Process the case.", exception)
        val errorResponse = ErrorResponse(message = GENERIC_ERROR_MESSAGE)
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    companion object {

        const val GENERIC_ERROR_MESSAGE = "Oops... Something went wrong."
    }

    data class ErrorResponse(
        val message: String,
        val timestamp: OffsetDateTime = OffsetDateTime.now(),
    )
}
