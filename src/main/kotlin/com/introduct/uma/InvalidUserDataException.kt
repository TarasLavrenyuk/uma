package com.introduct.uma

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidUserDataException(
    reason: String = "Invalid user data."
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    reason
)