package org.jmonster.codegenerator.web.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.jmonster.codegenerator.common.generator.EntityCodeGenerator
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByPlainTextRequestDto
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByTableRequestDto
import org.jmonster.codegenerator.common.monster.EntityCodeRestMonster
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/entity")
class EntityCodeGeneratorController(
    private val entityCodeGenerator: EntityCodeGenerator
) {

    @PostMapping("/table")
    fun generate(
        @Valid @RequestBody dto: EntityCodeGenerateByTableRequestDto,
        response: HttpServletResponse
    ): List<String> {
        return entityCodeGenerator.generate(EntityCodeRestMonster(dto))
    }

    @PostMapping("/text")
    fun generate(
        @Valid @RequestBody dto: EntityCodeGenerateByPlainTextRequestDto,
    ): List<String> {
        return entityCodeGenerator.generate(EntityCodeRestMonster(dto))
    }
}