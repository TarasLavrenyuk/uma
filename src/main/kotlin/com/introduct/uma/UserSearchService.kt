package com.introduct.uma

import com.introduct.uma.exceptions.InvalidArgumentException
import com.introduct.uma.web.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class UserSearchService(
    private val userRepo: UserRepo
) {

    fun searchUsers(
        pageable: Pageable
    ): Page<UserResponse> {
        validateSorting(pageable.sort)
        val userEntitiesPage: Page<UserEntity> = userRepo.findAll(pageable)
        return PageImpl(
            userEntitiesPage.content.map { UserResponse.fromUser(it) },
            pageable,
            userEntitiesPage.totalElements
        )
    }

    private fun validateSorting(sort: Sort) {
        sort.forEach {
            val propertyName = it.property
            if (propertyName !in UserSortProperties.getPropertyNames) {
                logger.warn("Wrong parameter passed - user search order: '${propertyName}'.")
                throw InvalidArgumentException(
                    "Unsupported sort property: '${propertyName}.'"
                )
            }
        }
    }

    enum class UserSortProperties(
        val propertyName: String
    ) {
        NAME("name"),
        CREATED_DATE("createdDate"),
        MODIFIED_DATE("modifiedDate")
        ;

        companion object {

            val getPropertyNames = values().map { it.propertyName }.toSet()
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(UserSearchService::class.java)
    }
}
