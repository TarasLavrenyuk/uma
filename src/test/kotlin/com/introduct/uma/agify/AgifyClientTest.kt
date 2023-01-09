package com.introduct.uma.agify

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

class AgifyClientTest {

    private val restTemplate: RestTemplate = mockk()

    private val agifyClient = AgifyClient(restTemplate)

    @BeforeEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `should return null when non 2xx code`() {
        every {
            restTemplate.exchange(any(), AgifyResponse::class.java)
        } throws RestClientResponseException(
            /* message = */ "error",
            /* statusCode = */ 400,
            /* statusText = */ "Bad Request",
            /* responseHeaders = */ null,
            /* responseBody = */ null,
            /* responseCharset = */ null
        )

        val result = agifyClient.getAgeForName("name")

        assertThat(result).isNull()

        val httpEntitySlot = slot<RequestEntity<Void>>()
        verify(exactly = 1) {
            restTemplate.exchange(capture(httpEntitySlot), AgifyResponse::class.java)
        }
        assertThat(httpEntitySlot.captured.method).isEqualTo(HttpMethod.GET)
        assertThat(httpEntitySlot.captured.url.toString()).isEqualTo("https://api.agify.io/?name=name")
    }

    @Test
    fun `should response entity`() {
        every {
            restTemplate.exchange(any(), AgifyResponse::class.java)
        } returns ResponseEntity.ok(
            AgifyResponse(
                age = 20,
                name = "naaame",
                count = 50
            )
        )

        val result = agifyClient.getAgeForName("naaame")

        assertThat(result!!.body!!.age).isEqualTo(20)
        assertThat(result.body!!.name).isEqualTo("naaame")
        assertThat(result.body!!.count).isEqualTo(50)

        val httpEntitySlot = slot<RequestEntity<Void>>()
        verify(exactly = 1) {
            restTemplate.exchange(capture(httpEntitySlot), AgifyResponse::class.java)
        }
        assertThat(httpEntitySlot.captured.method).isEqualTo(HttpMethod.GET)
        assertThat(httpEntitySlot.captured.url.toString()).isEqualTo("https://api.agify.io/?name=naaame")
    }
}