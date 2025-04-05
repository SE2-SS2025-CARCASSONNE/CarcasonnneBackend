package com.carcassonne.backend

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendApplication {

	private val logger = LoggerFactory.getLogger(BackendApplication::class.java)

	@PostConstruct
	fun showSwaggerUrl() {
		logger.info(" Swagger UI: http://localhost:8080/swagger-ui/index.html")
	}
}

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}