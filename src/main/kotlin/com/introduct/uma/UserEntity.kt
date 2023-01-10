package com.introduct.uma

import java.time.OffsetDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    var name: String,

    /**
     * Unique field
     */
    var email: String,

    var phone: String,

    var age: Int? = null,

    @CreatedDate
    val createdDate: OffsetDateTime = OffsetDateTime.now(),

    @LastModifiedDate
    val modifiedDate: OffsetDateTime = OffsetDateTime.now()
)
