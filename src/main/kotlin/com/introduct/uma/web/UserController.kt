package com.introduct.uma.web

import java.util.UUID
import com.introduct.uma.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        @RequestBody payload: CreateUserPayload
    ): ResponseEntity<UserResponse> {
        val userResponse = userService.createUser(payload = payload)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userResponse)
    }

    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody payload: UpdateUserPayload
    ): ResponseEntity<UserResponse> {
        val userResponse = userService.updateUser(
            userId = userId,
            payload = payload
        )
        return ResponseEntity.ok(userResponse)
    }

    // PUT /users/{id} - update user

    // DELETE /users/{id} - delete user by id
    // DELETE /users?ids=1,2,3 - delete multiple users by id

    // GET /users/{id} - get user by id
    // GET /users?limit=&offset=&sort=name_asc|name_desc|email_asc|age_asc|age_desc&name=&email=&phone=
    // find user by different criterias
}
