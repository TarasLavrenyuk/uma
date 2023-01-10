package com.introduct.uma

import java.util.UUID
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import com.introduct.uma.agify.AgifyService
import com.introduct.uma.exceptions.InvalidUserDataException
import com.introduct.uma.exceptions.UserNotFoundException
import com.introduct.uma.web.CreateUserPayload
import com.introduct.uma.web.UpdateUserPayload
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus

class UserServiceTest {

    private val userRepo: UserRepo = mockk()
    private val agifyService: AgifyService = mockk(relaxed = true)

    private val service = UserService(
        userRepo = userRepo,
        agifyService = agifyService
    )

    @BeforeEach
    fun cleanup() {
        clearAllMocks()

        every { userRepo.save(any()) } returnsArgument 0
        every { userRepo.countByEmail(any()) } returns 0
    }

    @Nested
    inner class CreateUserTest {

        @Test
        fun `should throw exception if email is invalid`() {
            assertThat {
                service.createUser(
                    CreateUserPayload(
                        name = "name",
                        email = "not_an_email",
                        phone = "12345",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("'not_an_email' is not a invalid email. Please correct.")
                }
        }

        @Test
        fun `should throw exception if name is blank`() {
            assertThat {
                service.createUser(
                    CreateUserPayload(
                        name = "   ",
                        email = "e@mail.com",
                        phone = "1234",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User name cannot be empty. Please correct.")
                }
        }

        @Test
        fun `should throw exception if phone is blank`() {
            assertThat {
                service.createUser(
                    CreateUserPayload(
                        name = "Name",
                        email = "e@mail.com",
                        phone = "    ",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User phone cannot be empty. Please correct.")
                }
        }

        @Test
        fun `should throw exception if email is taken`() {
            every { userRepo.countByEmail("e@mail.com") } returns 1

            assertThat {
                service.createUser(
                    CreateUserPayload(
                        name = "Name",
                        email = "e@mail.com",
                        phone = "123456",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User with email 'e@mail.com' already exists.")
                }
        }

        @Test
        fun `should trim name and phone when saving`() {
            service.createUser(
                CreateUserPayload(
                    name = "  Name   ",
                    email = "e@mail.com",
                    phone = "  123456   ",
                )
            )

            val newUserSlot = slot<UserEntity>()
            verify(exactly = 1) { userRepo.save(capture(newUserSlot)) }
            assertThat(newUserSlot.captured.name).isEqualTo("Name")
            assertThat(newUserSlot.captured.phone).isEqualTo("123456")
        }

        @Test
        fun `should get age by name when saving`() {
            service.createUser(
                CreateUserPayload(
                    name = "  Name   ",
                    email = "e@mail.com",
                    phone = "  123456   ",
                )
            )

            verify(exactly = 1) { agifyService.getAgeForName("Name") }
        }

        @Test
        fun `should save user with nullable age`() {
            every { agifyService.getAgeForName("Name") } returns null

            service.createUser(
                CreateUserPayload(
                    name = "  Name   ",
                    email = "e@mail.com",
                    phone = "  123456   ",
                )
            )

            verify(exactly = 1) { agifyService.getAgeForName("Name") }

            val newUserSlot = slot<UserEntity>()
            verify(exactly = 1) { userRepo.save(capture(newUserSlot)) }
            assertThat(newUserSlot.captured.age).isNull()
        }

        @Test
        fun `should save user`() {
            every { agifyService.getAgeForName("Name") } returns 50

            val result = service.createUser(
                CreateUserPayload(
                    name = "  Name   ",
                    email = "e@mail.com",
                    phone = "  123456   ",
                )
            )

            verify(exactly = 1) { agifyService.getAgeForName("Name") }

            assertThat(result.email).isEqualTo("e@mail.com")
            assertThat(result.name).isEqualTo("Name")
            assertThat(result.phone).isEqualTo("123456")
            assertThat(result.age).isEqualTo(50)
        }
    }

    @Nested
    inner class UpdateUserTest {

        @Test
        fun `should throw exception if user not found`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns null

            assertThat {
                service.updateUser(
                    userId = userId,
                    payload = UpdateUserPayload(
                        name = "name",
                        email = "not_an_email",
                        phone = "12345",
                    )
                )
            }.isFailure()
                .isInstanceOf(UserNotFoundException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                    assertThat(it.reason).isEqualTo("User not found. [userId=${userId}]")
                }
        }

        @Test
        fun `should throw exception if email is invalid`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns stubUser(userId)

            assertThat {
                service.updateUser(
                    userId = userId,
                    payload = UpdateUserPayload(
                        name = "New name",
                        email = "not_an_email",
                        phone = "12345",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("'not_an_email' is not a invalid email. Please correct.")
                }
        }

        @Test
        fun `should throw exception if name is blank`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns stubUser(userId)

            assertThat {
                service.updateUser(
                    userId = userId,
                    payload = UpdateUserPayload(
                        name = "           ",
                        email = "e@mail.com",
                        phone = "12345",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User name cannot be empty. Please correct.")
                }
        }

        @Test
        fun `should throw exception if phone is blank`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns stubUser(userId)

            assertThat {
                service.updateUser(
                    userId = userId,
                    payload = UpdateUserPayload(
                        name = "New name",
                        email = "e@mail.com",
                        phone = "       ",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User phone cannot be empty. Please correct.")
                }
        }

        @Test
        fun `should throw exception if email is taken`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns stubUser(userId)
            every { userRepo.countByEmail("e@mail.com") } returns 1

            assertThat {
                service.updateUser(
                    userId = userId,
                    payload = UpdateUserPayload(
                        name = "New name",
                        email = "e@mail.com",
                        phone = "12345678",
                    )
                )
            }.isFailure()
                .isInstanceOf(InvalidUserDataException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.BAD_REQUEST)
                    assertThat(it.reason).isEqualTo("User with email 'e@mail.com' already exists.")
                }
        }

        @Test
        fun `should only update not nullable fields`() {
            val userId = UUID.randomUUID()
            val existingUser = stubUser(userId)

            every { userRepo.findByIdOrNull(userId) } returns existingUser

            service.updateUser(
                userId = userId,
                payload = UpdateUserPayload(
                    name = "New name",
                    email = null,
                    phone = "123456789",
                )
            )

            val updatedUserSlot = slot<UserEntity>()
            verify(exactly = 1) { userRepo.save(capture(updatedUserSlot)) }

            assertThat(updatedUserSlot.captured.id).isEqualTo(userId)
            assertThat(updatedUserSlot.captured.name).isEqualTo("New name")
            assertThat(updatedUserSlot.captured.email).isEqualTo(existingUser.email)
            assertThat(updatedUserSlot.captured.phone).isEqualTo("123456789")
            assertThat(updatedUserSlot.captured.age).isEqualTo(existingUser.age)
        }

        @Test
        fun `should return updated user`() {
            val userId = UUID.randomUUID()
            val existingUser = stubUser(userId)

            every { userRepo.findByIdOrNull(userId) } returns existingUser

            val result = service.updateUser(
                userId = userId,
                payload = UpdateUserPayload(
                    name = "New name",
                    email = null,
                    phone = "123456789",
                )
            )

            val updatedUserSlot = slot<UserEntity>()
            verify(exactly = 1) { userRepo.save(capture(updatedUserSlot)) }

            assertThat(result.id).isEqualTo(userId)
            assertThat(result.name).isEqualTo("New name")
            assertThat(result.email).isEqualTo(existingUser.email)
            assertThat(result.phone).isEqualTo("123456789")
            assertThat(result.age).isEqualTo(existingUser.age)
        }
    }

    @Nested
    inner class DeleteUserTest {

        @Test
        fun `should throw exception if user not found`() {
            val userId = UUID.randomUUID()

            every { userRepo.deleteById(userId) } throws EmptyResultDataAccessException(1)

            assertThat {
                service.deleteUser(userId)
            }.isFailure()
                .isInstanceOf(UserNotFoundException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                    assertThat(it.reason).isEqualTo("User not found. [userId=${userId}]")
                }
        }

        @Test
        fun `should delete user`() {
            val userId = UUID.randomUUID()

            every { userRepo.deleteById(userId) } returns Unit

            assertThat { service.deleteUser(userId) }.isSuccess()
        }
    }

    @Nested
    inner class DeleteUsersTest {

        @Test
        fun `should throw exception if user not found`() {
            val userIds = setOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

            every { userRepo.deleteAllById(userIds) } throws EmptyResultDataAccessException(3)

            assertThat {
                service.deleteUsers(userIds)
            }.isFailure()
                .isInstanceOf(UserNotFoundException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                    assertThat(it.reason).isEqualTo("One or more users are not found by given ids.")
                }
        }

        @Test
        fun `should delete user`() {
            val userIds = setOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

            every { userRepo.deleteAllById(userIds) } returns Unit

            assertThat { service.deleteUsers(userIds) }.isSuccess()
        }
    }

    @Nested
    inner class GetUserTest {

        @Test
        fun `should throw exception if user not found`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns null

            assertThat {
                service.getUser(userId)
            }.isFailure()
                .isInstanceOf(UserNotFoundException::class.java)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                    assertThat(it.reason).isEqualTo("User not found. [userId=${userId}]")
                }
        }

        @Test
        fun `should delete user`() {
            val userId = UUID.randomUUID()

            every { userRepo.findByIdOrNull(userId) } returns stubUser(userId)

            val result = service.getUser(userId)
            assertThat(result.id).isEqualTo(userId)
        }
    }

    fun stubUser(
        id: UUID = UUID.randomUUID(),
        name: String = "User name",
        phone: String = "11111",
        email: String = "user@mail.com",
        age: Int? = null
    ) = UserEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        age = age
    )
}
