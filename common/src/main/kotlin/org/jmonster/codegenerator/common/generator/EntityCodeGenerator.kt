package org.jmonster.codegenerator.common.generator

import org.jmonster.codegenerator.common.model.dto.common.RestDto
import org.jmonster.codegenerator.common.model.dto.entity.Column
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByPlainTextRequestDto
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByTableRequestDto
import org.springframework.boot.ApplicationArguments
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


class EntityCodeGenerator : CodeGenerator {

    private var source: List<String> = listOf()
    private var destination: String = ""
    private var delimiter: String = "\t"
    private var tab: String = "\t"
    private var packageDeclaration: String = ""
    private var contentDto: MutableMap<String, List<Column>> = mutableMapOf()
    private var enversAuditMap: MutableMap<String, Boolean> = mutableMapOf()
    private val metaColumns = listOf("created_date", "created_user_id", "updated_date", "updated_user_id")
    private fun List<Column>.containsMetaColumn() = this.any { metaColumns.contains(it.name) }
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private var uniqueColumnCount: MutableMap<String, Int> = mutableMapOf()

    private fun composeCode(
        className: String,
        vararg unitGenerator: ((className: String) -> String),
    ): String {
        val code = buildString {
            unitGenerator.forEach {
                appendLine(it(className))
            }
        }
        return code
    }

    private fun generatePackageCode(className: String): String {
        return "package $packageDeclaration"
    }

    private fun generateImportCode(className: String): String {

        val thisContent = contentDto[className]
        requireNotNull(thisContent) { "Content Not Found" }

        val importCode = buildString {
            if (enversAuditMap[className] == true) appendLine("import org.hibernate.envers.Audited")

            appendLine("import java.util.*").appendLine("import javax.persistence.*")

            if (thisContent.containsMetaColumn()) appendLine("import org.springframework.data.jpa.domain.support.AuditingEntityListener")

            val types = thisContent.map { it.type }
            if (types.any { it.equals("timestamp", true) }) {
                appendLine("import java.time.LocalDateTime")
            }

            val columnsName = thisContent.map { it.name }
            if (columnsName.contains("updated_date")) appendLine("import org.springframework.data.annotation.LastModifiedDate")
            if (columnsName.contains("updated_user_id")) appendLine("import org.springframework.data.annotation.LastModifiedBy")
            if (columnsName.contains("created_date")) appendLine("import org.springframework.data.annotation.CreatedDate")
            if (columnsName.contains("created_user_id")) appendLine("import org.springframework.data.annotation.CreatedBy")

            if (thisContent.any { it.type.contains("JSONB", true) }) {
                appendLine("import org.hibernate.annotations.Type")
                    .appendLine("import org.hibernate.annotations.TypeDef")
                    .appendLine("import org.hibernate.annotations.TypeDefs")
                    .appendLine("import com.vladmihalcea.hibernate.type.json.JsonBinaryType")
            }
        }

        return importCode
    }

    private fun generateAnnotationCode(className: String): String {

        val thisContent = contentDto[className]
        requireNotNull(thisContent) { "Content Not Found" }

        val annotationCode = buildString {

            appendLine("@Entity")

            if (enversAuditMap[className] == true) appendLine("@Audited")
            val uniqueColumns = thisContent.filter { it.key.equals("UK", true) }

            if ((uniqueColumnCount[className] ?: 0) > 1) {
                val uniqueColumnsString = uniqueColumns.joinToString(",") { "\"${it.name}\"" }
                appendLine("@Table(name = \"tbl_${className.camelToSnakeCase()}\", uniqueConstraints = [UniqueConstraint(columnNames = [$uniqueColumnsString])])")
            } else {
                appendLine("@Table(name = \"tbl_${className.camelToSnakeCase()}\")")
            }

            if (thisContent.containsMetaColumn()) appendLine("@EntityListeners(AuditingEntityListener::class)")
            if ((thisContent.groupBy { it.key }["PK"]?.size
                    ?: 0) > 1
            ) appendLine("@IdClass(${className}PrimaryKey::class)")
            if (thisContent.any {
                    it.type.contains(
                        "JSONB",
                        true
                    )
                }) appendLine("@TypeDefs(TypeDef(name = \"jsonb\", typeClass = JsonBinaryType::class))")
        }

        return annotationCode

    }

    private fun generateConstructorCode(className: String): String {
        val constructorCode = buildString {
            appendLine("data class ${className}(")
            contentDto[className]
                ?.filterNot { metaColumns.contains(it.name) }
                ?.forEach { appendLine(getColumnDef(className, it)) }

            appendLine(")")
        }

        return constructorCode
    }

    private fun generateBodyCode(className: String): String {
        val bodyCode = buildString {

            val columns = contentDto[className]
                ?.filter { metaColumns.contains(it.name) }
                ?.map { getMetaColumnDef(className, it) }

            if (!columns.isNullOrEmpty()) {
                appendLine("{")
                columns.forEach { appendLine(it) }
                appendLine("}")
            }

            appendLine()
        }
        return bodyCode
    }

    private fun generatePkCode(className: String): String {
        val pkCode = buildString {
            val columnsMap = contentDto[className]?.groupBy { it.key } ?: emptyMap()
            val pkColumns = columnsMap["PK"] ?: emptyList()
            pkColumns.takeIf { it.size > 1 }
                ?.let { columns ->
                    appendLine("class ${className}PrimaryKey(")
                    columns.forEach { column -> appendLine("$tab${getPKColumn(column)}") }
                    appendLine(") : Serializable {")
                    appendLine("$tab${getCompositePKConstructor(pkColumns)}")
                    appendLine("}")
                }
        }

        return pkCode
    }

    private fun convertType(dbType: String): String {

        return when (val dbTypeUpperCase = dbType.uppercase()) {
            "UUID" -> "UUID"
            "JSONB(I18NNAMEMAP)" -> "I18nNameMap"
            "BOOL" -> "Boolean"
            "TIMESTAMP" -> "LocalDateTime"
            "DATETIME" -> "LocalDateTime"
            "DATE" -> "LocalDate"
            "TIME" -> "LocalTime"
            "TEXT" -> "String"
            "INT" -> "Int"
            "BIGINT" -> "Long"
            "DOUBLE PRECISION", "FLOAT8" -> "Double"
            "DECIMAL" -> "BigDecimal"
            "REAL" -> "Float"
            "BYTEA" -> "ByteArray"
            "ENUM" -> "String"
            "JSONB(LIST)" -> "List"
            "JSONB(MAP)" -> "Map"
            else -> handleTypesWithArguments(dbType)
        }

    }

    private fun handleTypesWithArguments(dbType: String): String =
        when {
            dbType.uppercase().contains("VARCHAR") -> "String"
            dbType.uppercase().contains("NUMERIC") || dbType.uppercase().contains("DECIMAL") -> "BigDecimal"
            else -> "String"
        }

    private fun getColumnDef(className: String, column: Column): String {
        val columnBlock = buildString {
            if (column.key.equals("PK", true)) appendLine("$tab@Id")
            if (column.type.uppercase().contains("JSONB", true)) appendLine("$tab@Type(type = \"jsonb\")")
            appendLine("$tab${getColumn(className, column)}")
            appendLine("$tab${getField(column)}")
        }
        return columnBlock
    }

    private fun getColumn(className: String, column: Column): String {
        return buildList {
            addNameDef(column)
            addVarcharLengthDef(column)
            addNumericPrecisionAndScaleDef(column)
            addNullableDef(column)
            addUpdatableDef(column)
            addUniqueDef(className, column)
            addJsonbColumnDef(column)
        }.let { "@Column(${it.joinToString(", ")})" }
    }

    private fun MutableList<String>.addNameDef(column: Column) {
        add("name = \"${column.name}\"")
    }

    private fun MutableList<String>.addVarcharLengthDef(column: Column) {
        if (column.type.contains("varchar", true)) {
            val regex = Regex("(\\d+)")
            regex.find(column.type)?.let {
                val (length) = it.groupValues.drop(0)
                add("length=$length")
            }
        }
    }

    private fun MutableList<String>.addNumericPrecisionAndScaleDef(column: Column) {
        if (column.type.contains("decimal", true) || column.type.contains("numeric", true)) {
            val regex = Regex("(\\d+),(\\d+)")
            regex.find(column.type)?.let {
                val values = it.groupValues
                val precision = values[1]
                val scale = values[2]
                add("precision=$precision")
                add("scale=$scale")
            }
        }
    }

    private fun MutableList<String>.addNullableDef(column: Column) {
        if (column.nullable.equals("notnull", true) && column.key != "PK") add("nullable = false")
    }

    private fun MutableList<String>.addUpdatableDef(column: Column) {
        if (column.name.equals("created_date", true) || column.name.equals("created_user_id", true)) {
            add("updatable = false")
        }
    }

    private fun MutableList<String>.addUniqueDef(className: String, column: Column) {
        if (uniqueColumnCount[className] == 1 && column.key.equals("UK", true)) {
            add("unique = true")
        }
    }

    private fun MutableList<String>.addJsonbColumnDef(column: Column) {
        if (column.type.uppercase().contains("JSONB", true)) {
            add("columnDefinition = \"jsonb\"")
        }
    }

    private fun getField(column: Column): String {
        val notNull = column.nullable.equals("notnull", true)
        return "var ${column.name.snakeToLowerCamelCase()}: ${convertType(column.type)}${if (notNull) "" else "?"},"
    }

    private fun getMetaColumnDef(className: String, column: Column): String {
        val code = buildString {
            when (column.name) {
                "updated_date" -> appendLine("$tab@LastModifiedDate")
                "updated_user_id" -> appendLine("$tab@LastModifiedBy")
                "created_date" -> appendLine("$tab@CreatedDate")
                "created_user_id" -> appendLine("$tab@CreatedBy")
            }
            appendLine("$tab${getColumn(className, column)}")
            appendLine("${tab}lateinit ${getField(column)}".replace(",", ""))
        }

        return code
    }

    private fun getPKColumn(column: Column) =
        "var ${column.name.snakeToLowerCamelCase()}: ${convertType(column.type)},"

    private fun getCompositePKConstructor(column: List<Column>) =
        "constructor() : this(${column.joinToString(", ") { convertToConstructorParam(it.type) }})"

    private fun convertToConstructorParam(dbType: String): String {
        return when (dbType.uppercase()) {
            "UUID" -> "UUID.randomUUID()"
            "JSONB(I18NNAMEMAP)" -> "I18nNameMap()"
            "BOOL" -> "false"
            "TIMESTAMP" -> "LocalDateTime.now()"
            "DATETIME" -> "LocalDateTime.now()"
            "DATE" -> "LocalDate.now()"
            "TIME" -> "LocalTime.now()"
            "TEXT" -> "\"\""
            "INT" -> "0"
            "BIGINT" -> "0"
            "DOUBLE PRECISION", "FLOAT8" -> "0.0"
            "DECIMAL" -> "BigDecimal.ZERO"
            "REAL" -> "0.0"
            "BYTEA" -> "ByteArray(0)"
            "ENUM" -> "\"\""
            else -> {
                "\"\""
            }
        }
    }


    fun String.camelToKebabCase(): String {

        return camelRegex.replace(this) {
            "-${it.value}"
        }.lowercase()
    }

    private fun String.camelToSnakeCase(): String {
        return camelRegex.replace(this) {
            "_${it.value}"
        }.lowercase()
    }

    private fun String.snakeToLowerCamelCase(): String {
        return snakeRegex.replace(this) {
            it.value.replace("_", "").uppercase()
        }
    }

    override fun generate(args: ApplicationArguments?) {
        when {
            args == null -> {
                println("Generate NOTHING")
                return
            }

            !preCheck(args) -> return
            else -> output()
        }
    }

    override fun generate(dto: RestDto): File {
        return when (dto) {
            is EntityCodeGenerateByTableRequestDto -> {
                generateByTable(dto)
            }

            is EntityCodeGenerateByPlainTextRequestDto -> {
                generateByPlainText(dto.tableName, dto.text, dto.enversAudit)
            }

            else -> {
                throw Exception("Unknown dto type")
            }
        }

    }

    private fun generateByTable(dto: EntityCodeGenerateByTableRequestDto): File {

        enversAuditMap[dto.tableName] = dto.enversAudit
        contentDto[dto.tableName] = dto.columns
        uniqueColumnCount[dto.tableName] = dto.columns.filter { it.key.equals("UK", true) }.size

        val code = composeCode(
            dto.tableName,
            this::generatePackageCode,
            this::generateImportCode,
            this::generateAnnotationCode,
            this::generateConstructorCode,
            this::generateBodyCode,
            this::generatePkCode
        )

        //TODO generate code in other languages
        val tempFile: Path = Files.createTempFile(dto.tableName, ".kt")
        Files.write(tempFile, listOf(code))
        return tempFile.toFile()
    }

    private fun generateByPlainText(tableName: String, text: String, enversAudit: Boolean): File {
        // [0] -> PK or UK or empty
        // [1] -> name
        // [2] -> type
        // [3] -> null or nonnull
        // [4] -> description
        enversAuditMap[tableName] = enversAudit
        val contentArg = text.split("\n").map { line ->
            line.split(delimiter).map { it.trim() }
        }
        contentDto[tableName] = contentArg.filter { it.size == 5 }.map { line ->
            Column(
                key = line[0].trim(),
                name = line[1].trim(),
                type = line[2].trim(),
                nullable = line[3].trim(),
                description = line[4].trim()
            )
        }
        uniqueColumnCount[tableName] = contentArg.filter { it[0].equals("UK", true) }.size

        val code = composeCode(
            tableName,
            this::generatePackageCode,
            this::generateImportCode,
            this::generateAnnotationCode,
            this::generateConstructorCode,
            this::generateBodyCode,
            this::generatePkCode
        )

        //TODO generate code in other languages
        val tempFile: Path = Files.createTempFile(tableName, ".kt")
        Files.write(tempFile, listOf(code))
        return tempFile.toFile()
    }

    private fun output() {

        source.forEach { s ->

            val separator = File.separator

            val file = File(s)
            val thisClassName = file.name.split(".")[0]
            var thisDestination = destination.ifBlank {
                s.substring(
                    0, s.lastIndexOf(separator)
                ) + "$separator$thisClassName$separator"
            }

            if (thisDestination[thisDestination.lastIndex].toString() != separator) thisDestination += separator

            if (destination.isBlank()) File(thisDestination).mkdirs()


            // [0] -> PK or UK or empty
            // [1] -> name
            // [2] -> type
            // [3] -> null or nonnull
            // [4] -> description
            val contentArg = file.readLines().map { line -> line.split(delimiter).map { it.trim() } }
            contentDto[thisClassName] = file.readLines().map { line ->
                val def = line.split(delimiter)
                Column(
                    key = def[0].trim(),
                    name = def[1].trim(),
                    type = def[2].trim(),
                    nullable = def[3].trim(),
                    description = def[4].trim()
                )
            }
            uniqueColumnCount[thisClassName] = contentArg.filter { it[0].equals("UK", true) }.size

            val code = composeCode(
                thisClassName,
                this::generatePackageCode,
                this::generateImportCode,
                this::generateAnnotationCode,
                this::generateConstructorCode,
                this::generateBodyCode,
                this::generatePkCode
            )


            File("${thisDestination}${thisClassName}.kt").bufferedWriter().use { out ->
                code.lines().forEach {
                    out.write(it)
                    out.newLine()
                }
            }

            println("Generated Entity: ${thisClassName}.kt")

        }
    }

    private fun preCheck(args: ApplicationArguments): Boolean {
        val sourceValid = preCheckSource(args)
        if (!sourceValid) return false
        val destinationValid = preCheckDestination(args)
        if (!destinationValid) return false
        preCheckDelimiter(args)
        preCheckPackage(args)
        preCheckEnverAudit(args)
        return true
    }

    private fun preCheckSource(args: ApplicationArguments): Boolean {
        source = args.getOptionValues("source")
        return if (source.isEmpty()) {
            println("Please declare --source")
            false
        } else {
            true
        }
    }

    private fun preCheckDestination(args: ApplicationArguments): Boolean {
        val destinationArgs = args.getOptionValues("destination")
        return if (destinationArgs != null && destinationArgs.size > 1) {
            println("Only one destination allowed")
            false
        } else {
            destination = destinationArgs?.firstOrNull() ?: ""
            true
        }
    }

    private fun preCheckPackage(args: ApplicationArguments): Boolean {
        val packageArgs = args.getOptionValues("package")
        packageDeclaration = packageArgs?.firstOrNull() ?: ""
        return true
    }

    private fun preCheckDelimiter(args: ApplicationArguments): Boolean {
        val delimiterArg = args.getOptionValues("delimiter")
        delimiter = if (delimiterArg.isNullOrEmpty()) "\t" else delimiterArg.first()
        return true
    }

    private fun preCheckEnverAudit(args: ApplicationArguments): Boolean {
        val enversAuditArg = args.getOptionValues("enversAudit")
        val source = args.getOptionValues("source")
        source.forEach {
            val thisClassName = it.split(".")[0]
            enversAuditMap[thisClassName] = enversAuditArg?.firstOrNull()?.toBoolean() ?: false
        }
        return true
    }

}



