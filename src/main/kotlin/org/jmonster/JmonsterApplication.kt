package org.jmonster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JmonsterApplication

fun main(args: Array<String>) {
	runApplication<JmonsterApplication>(*args)
}
