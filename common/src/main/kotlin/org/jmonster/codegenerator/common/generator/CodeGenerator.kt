package org.jmonster.codegenerator.common.generator

import org.jmonster.codegenerator.common.model.dto.common.RestDto
import org.springframework.boot.ApplicationArguments
import java.io.File

interface CodeGenerator {
    fun generate(args: ApplicationArguments?)
    fun generate(dto: RestDto): File
    fun generate(tableName: String, text: String): File
}