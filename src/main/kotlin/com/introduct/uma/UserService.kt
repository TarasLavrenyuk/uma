package com.introduct.uma

import com.introduct.uma.utils.StringUtils
import com.introduct.uma.web.CreateUserPayload
import com.introduct.uma.web.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepo: UserRepo
) {

    /**
     * @throws InvalidUserDataException if [payload] is invalid
     */
    fun createUser(
        payload: CreateUserPayload
    ): UserResponse {
        logger.debug("Trying to create new user.")
        validateNewUserPayload(payload)
        val newUser = UserEntity(
            name = payload.name,
            email = payload.email,
            phone = payload.phone,
        ).also {
            userRepo.save(it)
        }
        logger.debug("New user was successfully created. [userId=${newUser.id}]")
        return UserResponse.fromUser(newUser)
    }

    /**
     * @throws InvalidUserDataException if [payload] is invalid
     */
    private fun validateNewUserPayload(
        payload: CreateUserPayload
    ) {
        if (!StringUtils.isEmail(payload.email)) {
            throw InvalidUserDataException("'${payload.email}' is not a invalid email. Please correct.")
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}