package org.jmonster.codegenerator.common.model.dto.entity

data class Column(
    val key: String,
    val name: String,
    val type: String,
    val nullable: String,
    val description: String,
)