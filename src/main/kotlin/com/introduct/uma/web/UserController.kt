package com.introduct.uma.web

import java.util.UUID
import com.introduct.uma.UserSearchService
import com.introduct.uma.UserService
import com.introduct.uma.exceptions.InvalidArgumentException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
    private val userService: UserService,
    private val userSearchService: UserSearchService
) {

    @PostMapping
    @Operation(
        method = "POST",
        description = "Create user. All params are mandatory.",
        responses = [
            ApiResponse(
                responseCode = "201 CREATED",
                description = "User successfully updated."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "If input is incorrect. For example, email value is invalid.",
                useReturnTypeSchema = false
            )
        ]
    )
    fun createUser(
        @RequestBody payload: CreateUserPayload
    ): ResponseEntity<UserResponse> {
        val userResponse = userService.createUser(payload = payload)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userResponse)
    }

    @PutMapping("/{userId}")
    @Operation(
        method = "PUT",
        description = "Update user by id. All params are optional. If parameter is not specified, it will not be updated.",
        responses = [
            ApiResponse(
                responseCode = "200 OK",
                description = "User successfully updated."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "If input is incorrect. For example, email value is invalid.",
                useReturnTypeSchema = false
            ),
            ApiResponse(
                responseCode = "404 NOT FOUND",
                description = "If user with the give id is not found.",
                useReturnTypeSchema = false
            )
        ]
    )
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
    @Operation(
        method = "DELETE",
        description = "Delete user by id.",
        responses = [
            ApiResponse(
                responseCode = "204 NO CONTENT",
                description = "User successfully deleted."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "If id value is invalid."
            ),
            ApiResponse(
                responseCode = "404 NOT FOUND",
                description = "If user with the give id is not found."
            )
        ]
    )
    fun deleteUser(
        @PathVariable
        @Parameter(
            description = "User id to delete.",
            required = true,
            `in` = ParameterIn.PATH
        )
        userId: UUID
    ): ResponseEntity<Void> {
        userService.deleteUser(userId = userId)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    @DeleteMapping
    @Operation(
        method = "DELETE",
        description = "Delete multiple users by ids.",
        responses = [
            ApiResponse(
                responseCode = "204 NO CONTENT",
                description = "All users successfully deleted."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "If at least one id value is invalid."
            ),
            ApiResponse(
                responseCode = "404 NOT FOUND",
                description = "If at least one user with the give id is not found."
            )
        ]
    )
    fun deleteMultipleUsers(
        @RequestParam(name = "ids")
        @Parameter(
            description = "Comma separated list of user ids.",
            required = true,
            `in` = ParameterIn.QUERY
        )
        userIds: List<String>
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

    @GetMapping("/{userId}")
    @Operation(
        method = "GET",
        description = "Get user by id.",
        responses = [
            ApiResponse(
                responseCode = "200 OK",
                description = "User is found."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "If id value is invalid.",
                useReturnTypeSchema = false
            ),
            ApiResponse(
                responseCode = "404 NOT FOUND",
                description = "User with the given id does not exist.",
                useReturnTypeSchema = false
            )
        ]
    )
    fun getUser(
        @PathVariable
        @Parameter(
            required = true,
            `in` = ParameterIn.PATH
        )
        userId: UUID
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(userId = userId))
    }

    @GetMapping
    @Operation(
        method = "GET",
        description = "Search endpoint, has default pagination (see below) and can consume search params. Search params are combined with AND condition. " +
                "For example: /users?name=john&page=2&size=5&sort=name,desc will return 5 users from 11th to 15th that has 'john' in their names sorted by user name in descending order.",
        summary = "Search users endpoint.",
        responses = [
            ApiResponse(
                responseCode = "200 OK",
                description = "All params are ok. Also if no users found."
            ),
            ApiResponse(
                responseCode = "400 BAD REQUEST",
                description = "Invalid input. For example, 'sort' param is unsupported.",
                useReturnTypeSchema = false
            )
        ],
        parameters = [
            Parameter(
                name = "size",
                `in` = ParameterIn.QUERY,
                description = "Number of requested items",
                required = false,
                schema = Schema(
                    defaultValue = "${UserSearchService.DEFAULT_PAGE_SIZE}",
                    minimum = "0",
                    maximum = "${UserSearchService.MAX_PAGE_SIZE}"
                )
            ),
            Parameter(
                name = "page",
                `in` = ParameterIn.QUERY,
                description = "Number of the page. Starts with 0",
                required = false,
                schema = Schema(
                    defaultValue = "0",
                    minimum = "0"
                )
            ),
            Parameter(
                name = "sort",
                `in` = ParameterIn.QUERY,
                description = "Sorting order. Comma separated field name and order. See example.",
                required = false,
                example = "createdDate,desc",
                schema = Schema(
                    defaultValue = "name,asc",
                    allowableValues = ["name,asc", "createdDate,asc", "modifiedDate,asc", "name,desc", "createdDate,desc", "modifiedDate,desc"]
                )
            ),
            Parameter(
                name = "name",
                `in` = ParameterIn.QUERY,
                description = "Search param. If specified, only users with the given substring in their names will be returned. Case-insensitive param.",
                required = false
            ),
            Parameter(
                name = "email",
                `in` = ParameterIn.QUERY,
                description = "Search param. If specified only user with the exact given email will be returned. Case-sensitive param.",
                required = false
            ),
            Parameter(
                name = "phone",
                `in` = ParameterIn.QUERY,
                description = "Search param. If specified only user with the exact given phone will be returned. Case-sensitive param.",
                required = false
            )
        ]
    )
    fun searchUsers(
        @PageableDefault(
            size = UserSearchService.DEFAULT_PAGE_SIZE,
            page = 0,
            sort = ["name"],
        ) @Parameter(hidden = true) pageable: Pageable,
        @RequestParam(name = "name") name: String? = null,
        @RequestParam(name = "email") email: String? = null,
        @RequestParam(name = "phone") phone: String? = null,
    ): Page<UserResponse> {
        return userSearchService.searchUsers(
            pageable = pageable,
            name = name,
            email = email,
            phone = phone
        )
    }

    companion object {

        const val MAX_LIMIT_VALUE = 100

        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}
