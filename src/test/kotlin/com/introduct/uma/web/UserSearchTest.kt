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
    fun `should return paginated result`() {
        repeat(times = 5) {
            userRepo.save(
                UserEntity(
                    name = UUID.randomUUID().toString(),
                    email = "e$it@mail",
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
        val user1 = userRepo.save(UserEntity(name = "User C", email = "e@mail", phone = "123456"))
        val user2 = userRepo.save(UserEntity(name = "User A", email = "e@mail", phone = "123456"))
        val user3 = userRepo.save(UserEntity(name = "User E", email = "e@mail", phone = "123456"))
        val user4 = userRepo.save(UserEntity(name = "User B", email = "e@mail", phone = "123456"))
        val user5 = userRepo.save(UserEntity(name = "User D", email = "e@mail", phone = "123456"))

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

    private fun findUsers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = UserSearchService.UserSortProperties.NAME.propertyName,
        direction: Direction = Direction.ASC
    ): Page<UserResponse> {
        val response = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/users")
                .queryParam("page", page.toString())
                .queryParam("size", size.toString())
                .queryParam("sort", "$sortBy,${direction.name.lowercase()}")
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
