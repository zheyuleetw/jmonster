package org.jmonster.codegenerator.runner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JmonsterRunnerApplication

fun main(args: Array<String>) {
	runApplication<JmonsterRunnerApplication>(*args)
}
