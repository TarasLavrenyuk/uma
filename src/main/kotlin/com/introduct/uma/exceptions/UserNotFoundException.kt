package com.introduct.uma.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotFoundException(
    userId: String? = null,
    errorMessage: String? = null
) : ResponseStatusException(
    HttpStatus.NOT_FOUND,
    generateErrorMessage(userId, errorMessage)
) {
    companion object {

        private fun generateErrorMessage(
            userId: String?,
            errorMessage: String?
        ) = userId?.let { "User not found. [userId=${userId}]" }
            ?: errorMessage
            ?: "User not found."
    }
}