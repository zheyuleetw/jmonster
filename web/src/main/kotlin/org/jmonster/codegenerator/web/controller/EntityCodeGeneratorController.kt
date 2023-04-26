package org.jmonster.codegenerator.web.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.jmonster.codegenerator.common.generator.EntityCodeGenerator
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateRequestDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files


@RestController
@RequestMapping("/entity")
class EntityCodeGeneratorController {

    @PostMapping("/table")
    fun generate(
        @Valid @RequestBody dto: EntityCodeGenerateRequestDto,
        response: HttpServletResponse
    ): List<String> {
        val file = EntityCodeGenerator().generate(dto)
        return file.useLines { it.toList() }
    }

    @PostMapping("/text")
    fun generate(
        @Valid @RequestBody dto: String,
        @Valid @NotBlank @RequestParam tableName: String,
        response: HttpServletResponse
    ): ResponseEntity<ByteArray> {
        val file = EntityCodeGenerator().generate(tableName, dto)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.contentLength = file.length()
        headers.setContentDispositionFormData("attachment", "$tableName.kt")
        return ResponseEntity.ok().headers(headers).body(Files.readAllBytes(file.toPath()));
    }
}