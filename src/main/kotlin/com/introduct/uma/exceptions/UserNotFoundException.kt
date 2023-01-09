package com.introduct.uma.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotFoundException(
    userId: String? = null
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    generateErrorMessage(userId)
) {
    companion object {

        private fun generateErrorMessage(
            userId: String?
        ) = if (userId != null) "User not found. [userId=${userId}]" else "User not found."
    }
}