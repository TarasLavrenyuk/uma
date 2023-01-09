package com.introduct.uma.web

import java.util.UUID
import com.introduct.uma.UserService
import com.introduct.uma.exceptions.InvalidArgumentException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: UUID
    ): ResponseEntity<Void> {
        userService.deleteUser(userId = userId)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping
    fun deleteMultipleUsers(
        @RequestParam(name = "ids") userIds: List<String>
    ): ResponseEntity<Void> {
        if (userIds.size > MAX_LIMIT_VALUE) {
            logger.warn("Trying to delete more than $MAX_LIMIT_VALUE users.")
            throw InvalidArgumentException("Unable to delete more than $MAX_LIMIT_VALUE at once.")
        }
        userService.deleteUsers(userIds = userIds.toUUIDs())
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    private fun List<String>.toUUIDs(): List<UUID> = map {
        try {
            UUID.fromString(it)
        } catch (e: IllegalArgumentException) {
            throw InvalidArgumentException("Invalid id value: $it")
        }
    }

    // GET /users/{id} - get user by id
    // GET /users?limit=&offset=&sort=name_asc|name_desc|email_asc|age_asc|age_desc&name=&email=&phone=
    // find user by different criterias

    companion object {

        const val MAX_LIMIT_VALUE = 100

        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}
