package com.introduct.uma

import java.util.UUID
import com.introduct.uma.agify.AgifyService
import com.introduct.uma.exceptions.InvalidUserDataException
import com.introduct.uma.exceptions.UserNotFoundException
import com.introduct.uma.utils.StringUtils
import com.introduct.uma.web.CreateUserPayload
import com.introduct.uma.web.UpdateUserPayload
import com.introduct.uma.web.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepo: UserRepo,
    private val agifyService: AgifyService
) {

    /**
     * @throws InvalidUserDataException if [payload] is invalid
     */
    fun createUser(
        payload: CreateUserPayload
    ): UserResponse {
        logger.debug("Trying to create new user.")
        validateNewUserPayload(payload)

        if (userRepo.countByEmail(payload.email.trim()) > 0) {
            throw InvalidUserDataException("User with email '${payload.email.trim()}' already exists.")
        }

        val newUser = UserEntity(
            name = payload.name.trim(),
            email = payload.email,
            phone = payload.phone.trim(),
            age = agifyService.getAgeForName(payload.name.trim())
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
        val existingUserEntity = userRepo.findByIdOrNull(userId)
            ?: throw UserNotFoundException(userId = userId.toString())

        validateUpdateUserPayload(payload)

        payload.email?.let { newEmail ->
            if (userRepo.countByEmail(newEmail.trim()) > 0) {
                throw InvalidUserDataException("User with email '${payload.email.trim()}' already exists.")
            }
        }

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
     * @throws UserNotFoundException if [UserEntity] with [userId] doesn't exist
     */
    fun deleteUser(
        userId: UUID
    ) = try {
        userRepo.deleteById(userId)
        logger.debug("User successfully deleted. [userId=${userId}]")
    } catch (e: EmptyResultDataAccessException) {
        logger.warn("Trying to delete user. User not found. [userId=${userId}]", e)
        throw UserNotFoundException(userId = userId.toString())
    }

    fun deleteUsers(userIds: Collection<UUID>) = try {
        userRepo.deleteAllById(userIds)
    } catch (e: EmptyResultDataAccessException) {
        logger.warn("Trying to delete users. One or more users are not found.", e)
        throw UserNotFoundException(errorMessage = "One or more users are not found by given ids.")
    }

    /**
     * @throws UserNotFoundException if with id [userId] does not exist
     */
    fun getUser(userId: UUID): UserResponse {
        val userEntity = userRepo.findByIdOrNull(userId) ?: throw UserNotFoundException(userId.toString())
        return UserResponse.fromUser(userEntity)
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
        if (payload.name.isBlank()) {
            throw InvalidUserDataException("User name cannot be empty. Please correct.")
        }
        if (payload.phone.isBlank()) {
            throw InvalidUserDataException("User phone cannot be empty. Please correct.")
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
        payload.name?.let {
            if (it.isBlank()) {
                throw InvalidUserDataException("User name cannot be empty. Please correct.")
            }
        }
        payload.phone?.let {
            if (it.isBlank()) {
                throw InvalidUserDataException("User phone cannot be empty. Please correct.")
            }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
