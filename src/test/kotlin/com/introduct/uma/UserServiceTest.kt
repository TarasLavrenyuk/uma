package com.introduct.uma

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.introduct.uma.agify.AgifyService
import com.introduct.uma.exceptions.InvalidUserDataException
import com.introduct.uma.web.CreateUserPayload
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
}
