package org.jmonster.codegenerator.common.model.dto.entity

import jakarta.validation.constraints.NotBlank
import org.jmonster.codegenerator.common.model.dto.common.RestDto

data class EntityCodeGenerateByPlainTextRequestDto(
    @field:NotBlank(message = "Please input table name")
    val tableName: String,
    val enversAudit: Boolean = true,
    @field:NotBlank(message = "text can not be empty")
    val text: String
) : RestDto