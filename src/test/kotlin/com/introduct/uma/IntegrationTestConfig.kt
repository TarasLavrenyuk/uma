package com.introduct.uma

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@AutoConfigureMockMvc
open class IntegrationTestConfig {

    companion object {

        @Container
        val container: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:12.1")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
    }
}
