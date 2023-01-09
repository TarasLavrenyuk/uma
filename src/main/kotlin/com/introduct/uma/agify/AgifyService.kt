package com.introduct.uma.agify

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AgifyService(
    private val client: AgifyClient
) {

    fun getAgeForName(
        name: String
    ): Int? {
        val response = client.getAgeForName(name)
        if (response == null) {
            logger.warn("Unable to fetch age for name. [name=${name}]")
            return null
        }
        val body = response.body
        if (body?.age == null) {
            logger.warn("There is no age for name. [name=${name}]")
            return null
        }

        val result = body.age
        logger.debug("Age for name successfully found. [name=${name}] [age={$result}]")
        return result
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AgifyClient::class.java)
    }
}