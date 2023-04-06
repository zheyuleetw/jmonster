package org.jmoster.generator

import org.springframework.boot.ApplicationArguments

interface CodeGenerator {
    fun generate(args: ApplicationArguments?)
}