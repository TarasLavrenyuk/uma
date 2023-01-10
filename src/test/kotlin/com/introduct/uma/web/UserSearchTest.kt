package com.introduct.uma.web

import java.util.UUID
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.introduct.uma.IntegrationTestConfig
import com.introduct.uma.UserEntity
import com.introduct.uma.UserRepo
import com.introduct.uma.UserSearchService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort.Direction
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
class UserSearchTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val userRepo: UserRepo
) : IntegrationTestConfig() {

    @BeforeEach
    fun cleanupDb() {
        userRepo.deleteAll()
    }

    @Test
    fun `pagination should work`() {
        repeat(times = 5) {
            userRepo.save(
                UserEntity(
                    name = UUID.randomUUID().toString(),
                    email = "e$it@mail.com",
                    phone = "123456$it"
                )
            )
        }
        assertThat(userRepo.findAll()).hasSize(5)

        val page1 = findUsers(page = 0, size = 2)
        verifyPage(page1, 0, 2, true, 5)

        val page2 = findUsers(page = 1, size = 2)
        verifyPage(page2, 1, 2, true, 5)

        val page3 = findUsers(page = 2, size = 2)
        verifyPage(page3, 2, 1, false, 5)
    }

    @Test
    fun `sorting should work`() {
        val user1 = userRepo.save(UserEntity(name = "User C", email = "ec@mail.com", phone = "123456"))
        val user2 = userRepo.save(UserEntity(name = "User A", email = "ea@mail.com", phone = "123456"))
        val user3 = userRepo.save(UserEntity(name = "User E", email = "ee@mail.com", phone = "123456"))
        val user4 = userRepo.save(UserEntity(name = "User B", email = "eb@mail.com", phone = "123456"))
        val user5 = userRepo.save(UserEntity(name = "User D", email = "ed@mail.com", phone = "123456"))

        assertThat(userRepo.findAll()).hasSize(5)

        val page = findUsers(
            size = 10,
            sortBy = "name",
            direction = Direction.DESC
        )
        val users = page.content
        assertThat(users).hasSize(5)
        assertThat(users.map { it.id }).containsExactly(user3.id, user5.id, user1.id, user4.id, user2.id)
    }

    @Test
    fun `should search users by name`() {
        val userJohn = userRepo.save(UserEntity(name = "John", email = "john@mail.com", phone = "123456"))
        val userJohnathan = userRepo.save(
            UserEntity(name = "Johnathan", email = "johnathan@mail.com", phone = "123456")
        )
        val userJames = userRepo.save(UserEntity(name = "James", email = "james@mail.com", phone = "123456"))

        val page = findUsers(
            name = "John",
            sortBy = "name",
            direction = Direction.DESC
        )
        verifyPage(
            actual = page,
            expectedPage = 0,
            expectedSize = 2,
            expectedHasNext = false,
            expectedTotal = 2,
        )
        assertThat(page.content.map { it.id }).containsExactly(userJohnathan.id, userJohn.id)
    }

    @Test
    fun `should search users by part of the name`() {
        val userJohn = userRepo.save(UserEntity(name = "John", email = "john@mail.com", phone = "123456"))
        val userJohnathan =
            userRepo.save(UserEntity(name = "Johnathan", email = "johnathan@mail.com", phone = "123456"))
        val userJames = userRepo.save(UserEntity(name = "James", email = "james@mail.com", phone = "123456"))

        val page = findUsers(name = "nathan")
        verifyPage(
            actual = page,
            expectedPage = 0,
            expectedSize = 1,
            expectedHasNext = false,
            expectedTotal = 1,
        )
        assertThat(page.content.first().id).isEqualTo(userJohnathan.id)
    }

    @Test
    fun `should search users by exact email match`() {
        val userJohn = userRepo.save(UserEntity(name = "John", email = "john@mail.com", phone = "123456"))
        val userJohnathan =
            userRepo.save(UserEntity(name = "Johnathan", email = "johnathan@mail.com", phone = "123456"))
        val userJames = userRepo.save(UserEntity(name = "James", email = "james@mail.com", phone = "123456"))

        val page1 = findUsers(email = "james@mail.com")
        verifyPage(
            actual = page1,
            expectedPage = 0,
            expectedSize = 1,
            expectedHasNext = false,
            expectedTotal = 1,
        )
        assertThat(page1.content.first().id).isEqualTo(userJames.id)

        val page2 = findUsers(email = "@mail.com")
        verifyPage(
            actual = page2,
            expectedPage = 0,
            expectedSize = 0,
            expectedHasNext = false,
            expectedTotal = 0,
        )
    }

    @Test
    fun `should search users by exact phone match`() {
        val userJohn = userRepo.save(UserEntity(name = "John", email = "john@mail.com", phone = "1234"))
        val userJohnathan = userRepo.save(UserEntity(name = "Johnathan", email = "johnathan@mail.com", phone = "12345"))
        val userJames = userRepo.save(UserEntity(name = "James", email = "james@mail.com", phone = "123456"))

        val page1 = findUsers(phone = "1234")
        verifyPage(
            actual = page1,
            expectedPage = 0,
            expectedSize = 1,
            expectedHasNext = false,
            expectedTotal = 1,
        )
        assertThat(page1.content.first().id).isEqualTo(userJohn.id)

        val page2 = findUsers(phone = "234")
        verifyPage(
            actual = page2,
            expectedPage = 0,
            expectedSize = 0,
            expectedHasNext = false,
            expectedTotal = 0,
        )
    }

    @Test
    fun `should search by combined filters`() {
        val userJohn = userRepo.save(UserEntity(name = "John", email = "john@mail.com", phone = "1234"))
        val userJohnathan = userRepo.save(UserEntity(name = "Johnathan", email = "johnathan@mail.com", phone = "12345"))
        val userJames = userRepo.save(UserEntity(name = "James", email = "james@mail.com", phone = "123456"))

        val page1 = findUsers(
            name = "john",
            phone = "12345"
        )
        verifyPage(
            actual = page1,
            expectedPage = 0,
            expectedSize = 1,
            expectedHasNext = false,
            expectedTotal = 1,
        )
        assertThat(page1.content.first().id).isEqualTo(userJohnathan.id)

        val page2 = findUsers(
            name = "john",
            phone = "1234"
        )
        verifyPage(
            actual = page2,
            expectedPage = 0,
            expectedSize = 1,
            expectedHasNext = false,
            expectedTotal = 1,
        )
        assertThat(page2.content.first().id).isEqualTo(userJohn.id)
    }

    private fun findUsers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = UserSearchService.UserSortProperties.NAME.propertyName,
        direction: Direction = Direction.ASC,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
    ): Page<UserResponse> {
        val response = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/users")
                .queryParam("page", page.toString())
                .queryParam("size", size.toString())
                .queryParam("sort", "$sortBy,${direction.name.lowercase()}")
                .queryParam("name", name)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        return objectMapper.readValue(
            response.contentAsString,
            object : TypeReference<RestResponsePage<UserResponse>>() {}
        )!!
    }

    fun verifyPage(
        actual: Page<UserResponse>,
        expectedPage: Int,
        expectedSize: Int,
        expectedHasNext: Boolean,
        expectedTotal: Long
    ) {
        assertThat(actual.number).isEqualTo(expectedPage)
        assertThat(actual.content.size).isEqualTo(expectedSize)
        assertThat(actual.hasNext()).isEqualTo(expectedHasNext)
        assertThat(actual.totalElements).isEqualTo(expectedTotal)
    }
}
