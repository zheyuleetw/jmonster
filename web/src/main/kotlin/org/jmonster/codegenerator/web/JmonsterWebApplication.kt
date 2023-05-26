package org.jmonster.codegenerator.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(value = ["org.jmonster.codegenerator.common", "org.jmonster.codegenerator.web"])
class JmonsterApplication

fun main(args: Array<String>) {
    runApplication<JmonsterApplication>(*args)
}
