package com.introduct.uma.agify

import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Component
class AgifyClient(
    private val restTemplate: RestTemplate
) {

    fun getAgeForName(
        name: String
    ): ResponseEntity<AgifyResponse>? {
        val httpEntity = RequestEntity<Void>(
            HttpMethod.GET,
            URI("$AGIFY_BASE_URL/?name=$name")
        )

        return try {
            restTemplate.exchange(httpEntity, AgifyResponse::class.java)
        } catch (e: RestClientResponseException) {
            logger.warn("Error during agify-get-name request.", e)
            null
        }
    }

    companion object {

        private const val AGIFY_BASE_URL = "https://api.agify.io"

        private val logger = LoggerFactory.getLogger(AgifyClient::class.java)
    }
}