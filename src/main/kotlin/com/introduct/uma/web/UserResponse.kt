package com.introduct.uma.web

import java.util.UUID
import com.introduct.uma.UserEntity


data class UserResponse(

    val id: UUID,
    val name: String,
    val email: String,
    val phone: String,
    val age: Int?
) {

    companion object {
        fun fromUser(user: UserEntity) = UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            age = user.age,
        )
    }
}
