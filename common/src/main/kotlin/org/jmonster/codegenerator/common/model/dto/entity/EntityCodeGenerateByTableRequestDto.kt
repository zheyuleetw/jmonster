package org.jmonster.codegenerator.common.model.dto.entity

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.jmonster.codegenerator.common.model.dto.common.RestDto

data class EntityCodeGenerateByTableRequestDto(
    @field:NotBlank(message = "Please input table name")
    val tableName: String,
    val enversAudit: Boolean = true,
    @field:NotEmpty(message = "Please add not least one column")
    val columns: List<Column>
) : RestDto