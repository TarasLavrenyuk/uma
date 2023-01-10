package com.introduct.uma

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface UserRepo : JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    fun countByEmail(email: String): Int
}