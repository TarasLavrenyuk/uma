package com.introduct.uma.web

import com.introduct.uma.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(
        @RequestBody userPayload: CreateUserPayload
    ): ResponseEntity<UserResponse> {
        val userResponse = userService.createUser(payload = userPayload)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userResponse)
    }
}
