package com.introduct.uma

import com.introduct.uma.exceptions.InvalidArgumentException
import com.introduct.uma.web.UserResponse
import javax.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

@Service
class UserSearchService(
    private val userRepo: UserRepo
) {

    /**
     * @throws InvalidArgumentException input is incorrect, for example [Pageable.getPageNumber] is negative, etc.
     */
    fun searchUsers(
        pageable: Pageable,
        name: String?,
        email: String?,
        phone: String?
    ): Page<UserResponse> {
        validateSorting(pageable.sort)
        validatePagination(pageable.pageNumber, pageable.pageSize)
        val userEntitiesPage: Page<UserEntity> = userRepo.findAll(
            buildSpecification(name, email, phone),
            pageable
        )
        return PageImpl(
            userEntitiesPage.content.map { UserResponse.fromUser(it) },
            pageable,
            userEntitiesPage.totalElements
        )
    }

    private fun validatePagination(pageNumber: Int, pageSize: Int) {
        if (pageNumber < 0) {
            throw InvalidArgumentException("Invalid page number param.")
        }
        if (pageSize < 0 || pageSize > MAX_PAGE_SIZE) {
            throw InvalidArgumentException("Invalid page size param. Max page size: $MAX_PAGE_SIZE.")
        }
    }

    private fun validateSorting(sort: Sort) {
        sort.forEach {
            val propertyName = it.property
            if (propertyName !in UserSortProperties.getPropertyNames) {
                logger.warn("Wrong parameter passed - user search order: '${propertyName}'.")
                throw InvalidArgumentException(
                    "Unsupported sort property: '${propertyName}'."
                )
            }
        }
    }

    private fun buildSpecification(
        name: String?,
        email: String?,
        phone: String?
    ): Specification<UserEntity> = Specification<UserEntity> { root, _, cb ->
        val predicates = mutableListOf<Predicate>()
        name?.let { predicates.add(cb.like(cb.upper(root.get("name")), "%${it.trim().uppercase()}%")) }
        email?.let { predicates.add(cb.equal(root.get<UserEntity>("email"), it.trim())) }
        phone?.let { predicates.add(cb.equal(root.get<UserEntity>("phone"), it.trim())) }

        return@Specification cb.and(*predicates.toTypedArray())
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

        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 100

        private val logger = LoggerFactory.getLogger(UserSearchService::class.java)
    }
}
