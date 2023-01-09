package com.introduct.uma.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidArgumentException(
    message: String = "Invalid input."
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    message
)
