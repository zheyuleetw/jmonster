package org.jmoster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EntityCodeGeneratorApplication

fun main(args: Array<String>) {
    runApplication<EntityCodeGeneratorApplication>(*args)
}