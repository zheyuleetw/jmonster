package org.jmonster.codegenerator.generator

import org.jmoster.model.dto.common.RestDto
import org.springframework.boot.ApplicationArguments
import java.io.File

interface CodeGenerator {
    fun generate(args: ApplicationArguments?)
    fun generate(dto: RestDto): File
    fun generate(tableName: String, text: String): File
}