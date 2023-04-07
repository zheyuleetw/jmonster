package org.jmonster.codegenerator.generator

import org.springframework.boot.ApplicationArguments

interface CodeGenerator {
    fun generate(args: ApplicationArguments?)
}