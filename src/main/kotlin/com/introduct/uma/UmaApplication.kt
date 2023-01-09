package com.introduct.uma

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UmaApplication

fun main(args: Array<String>) {
	runApplication<UmaApplication>(*args)
}
