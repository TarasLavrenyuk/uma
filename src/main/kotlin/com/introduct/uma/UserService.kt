package com.introduct.uma

import java.util.UUID
import com.introduct.uma.exceptions.InvalidUserDataException
import com.introduct.uma.exceptions.UserNotFoundException
import com.introduct.uma.utils.StringUtils
import com.introduct.uma.web.CreateUserPayload
import com.introduct.uma.web.UpdateUserPayload
import com.introduct.uma.web.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
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
     * @throws UserNotFoundException if [UserEntity] with [userId] doesn't exist
     */
    fun updateUser(
        userId: UUID,
        payload: UpdateUserPayload
    ): UserResponse {
        logger.debug("Trying to update user. [userId=${userId}]")
        val existingUserEntity = userRepo.findByIdOrNull(userId) ?: throw UserNotFoundException(userId.toString())
        validateUpdateUserPayload(payload)

        existingUserEntity.apply {
            name = payload.name ?: this@apply.name
            email = payload.email ?: this@apply.email
            phone = payload.phone ?: this@apply.phone
        }.also {
            userRepo.save(it)
        }

        logger.debug("User was successfully updated. [userId=${userId}]")

        return UserResponse.fromUser(existingUserEntity)
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

    /**
     * @throws InvalidUserDataException if [payload] is invalid
     */
    private fun validateUpdateUserPayload(
        payload: UpdateUserPayload
    ) {
        payload.email?.let {
            if (!StringUtils.isEmail(it)) {
                throw InvalidUserDataException("'${payload.email}' is not a invalid email. Please correct.")
            }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
