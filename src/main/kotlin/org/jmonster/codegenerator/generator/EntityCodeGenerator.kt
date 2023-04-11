package org.jmonster.codegenerator.generator

import org.jmonster.codegenerator.model.dto.common.RestDto
import org.jmonster.codegenerator.model.dto.entity.EntityCodeGenerateRequestDto
import org.springframework.boot.ApplicationArguments
import java.io.File
import java.nio.file.Files
import java.nio.file.Path


class EntityCodeGenerator : CodeGenerator {

    private var source: List<String> = listOf()
    private var destination: String = ""
    private var delimiter: String = ""
    private var packageDeclaration: String = ""
    private var content: MutableMap<String, List<List<String>>> = mutableMapOf()
    private var enversAudit: Boolean = false
    private val metaColumns = listOf("created_date", "created_user_id", "updated_date", "updated_user_id")
    private fun List<List<String>>.hasMetaColumn() = this.any { metaColumns.contains(it[1]) }
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private var uniqueColumnCount: MutableMap<String, Int> = mutableMapOf()

    private fun composeCode(
        className: String,
        vararg unitGenerator: ((className: String) -> String),
    ): String {
        val code = StringBuilder()
        unitGenerator.forEach { it ->
            code.appendLine(it(className))
        }

        return code.toString()

    }

    private fun generatePackageCode(className: String): String {
        return "package $packageDeclaration"
    }

    private fun generateImportCode(className: String): String {

        val thisContent = content[className] ?: throw Exception("Content Not Found")

        val importCode = StringBuilder()
        if (enversAudit) {
            importCode.appendLine("import org.hibernate.envers.Audited")
        }
        importCode.appendLine("import java.util.*").appendLine("import javax.persistence.*")

        if (thisContent.hasMetaColumn()) importCode.appendLine("import org.springframework.data.jpa.domain.support.AuditingEntityListener")
        if (thisContent.map { it[2] }
                .any { type -> type.equals("timestamp", true) }) importCode.appendLine("import java.time.LocalDateTime")

        val columnsName = thisContent.map { it[1] }
        if (columnsName.contains("updated_date")) importCode.appendLine("import org.springframework.data.annotation.LastModifiedDate")
        if (columnsName.contains("updated_user_id")) importCode.appendLine("import org.springframework.data.annotation.LastModifiedBy")
        if (columnsName.contains("created_date")) importCode.appendLine("import org.springframework.data.annotation.CreatedDate")
        if (columnsName.contains("created_user_id")) importCode.appendLine("import org.springframework.data.annotation.CreatedBy")
        if (thisContent.any { it[2].equals("JSONB", true) }) {
            importCode.appendLine("import org.hibernate.annotations.Type")
                .appendLine("import org.hibernate.annotations.TypeDef")
                .appendLine("import org.hibernate.annotations.TypeDefs")
                .appendLine("import com.vladmihalcea.hibernate.type.json.JsonBinaryType")
        }

        return importCode.toString()
    }

    private fun generateAnnotationCode(className: String): String {

        val thisContent = content[className] ?: throw Exception("Content Not Found")

        val annotationCode = StringBuilder().appendLine("@Entity")

        if (enversAudit) {
            annotationCode.appendLine("@Audited")
        }

        val uniqueColumns = thisContent.filter { it[0].equals("UK", true) }

        if ((uniqueColumnCount[className] ?: 0) > 1) {
            val uniqueColumnsString = uniqueColumns.joinToString(",") { "\"${it[1]}\"" }
            annotationCode.appendLine("@Table(name = \"tbl_${className.camelToSnakeCase()}\", uniqueConstraints = [UniqueConstraint(columnNames = [$uniqueColumnsString])])")
        } else {
            annotationCode.appendLine("@Table(name = \"tbl_${className.camelToSnakeCase()}\")")
        }

        if (thisContent.hasMetaColumn()) annotationCode.appendLine("@EntityListeners(AuditingEntityListener::class)")
        if ((thisContent.groupBy { it[0] }["PK"]?.size ?: 0) > 1) {
            annotationCode.appendLine("@IdClass(${className}PrimaryKey::class)")
        }
        if (thisContent.any { it[2].equals("JSONB", true) }) {
            annotationCode.appendLine("@TypeDefs(TypeDef(name = \"jsonb\", typeClass = JsonBinaryType::class))")
        }

        return annotationCode.toString()

    }

    fun generateConstructorCode(className: String): String {
        val constructorCode = StringBuilder()
        constructorCode.appendLine("data class ${className}(")
        content[className]?.filter { !metaColumns.contains(it[1]) }
            ?.forEach { constructorCode.appendLine(getColumnDef(className, it)) }
        constructorCode.appendLine(")")
        return constructorCode.toString()
    }

    fun generateBodyCode(className: String): String {
        val bodyCode = StringBuilder()
        bodyCode.appendLine("{")
        content[className]?.filter { metaColumns.contains(it[1]) }
            ?.forEach { bodyCode.appendLine(getMetaColumnDef(className, it)) }
        bodyCode.appendLine("}").appendLine()
        return bodyCode.toString()

    }

    fun generatePkCode(className: String): String {
        val pkCode = StringBuilder()
        val map = content[className]?.groupBy { it[0] } ?: emptyMap()
        if ((map["PK"]?.size ?: 0) > 1) {
            pkCode.appendLine("class ${className}PrimaryKey(")
            map["PK"]!!.forEach { pkCode.appendLine(getPKColumn(it)) }
            pkCode.appendLine(") : Serializable {").appendLine(getCompositePKConstructor(map["PK"]!!)).appendLine("}")
        }
        return pkCode.toString()

    }

    private fun convertType(dbType: String): String {

        return when (dbType.uppercase()) {
            "UUID" -> "UUID"
            "JSONB" -> "I18nNameMap"
            "BOOL" -> "Boolean"
            "TIMESTAMP" -> "LocalDateTime"
            "DATETIME" -> "LocalDateTime"
            "TEXT" -> "String"
            "TIME" -> "LocalTime"
            "INT" -> "Int"
            else -> {
                "String"
            }
        }

    }

    private fun getColumnDef(className: String, column: List<String>): String {
        val columnBlock = StringBuilder()
        columnBlock.appendLine(if (column[0].equals("PK", true)) "@Id" else "")
            .appendLine(if (column[2].equals("JSONB", true)) "@Type(type = \"jsonb\")" else "")
            .appendLine(getColumn(className, column)).appendLine(getField(column))
        return columnBlock.toString()
    }

    private fun getColumn(className: String, column: List<String>): String {

        val def = mutableListOf<String>()

        def.add("name = \"${column[1]}\"")

        val notNull = column[3].equals("notnull", true)


        if (column[2].contains("varchar", true)) def.add(
            // example: get 200 from varchar(200)
            "length = ${
                column[2].substring(column[2].indexOf("(") + 1).replace(")", "")
            }"
        )

        if (notNull && column[0] != "PK") def.add("nullable = false")

        if (column[1].equals("created_date", true) || column[1].equals(
                "created_user_id", true
            )
        ) def.add("updatable = false")

        if (uniqueColumnCount[className] == 1 && column[0].equals("UK", true)) {
            def.add("unique = true")
        }

        if (column[2].equals("JSONB", true)) def.add("columnDefinition = \"jsonb\"")

        return "@Column(${def.joinToString(", ")})"
    }

    private fun getField(column: List<String>): String {
        val notNull = column[3].equals("notnull", true)
        return "var ${column[1].snakeToLowerCamelCase()}: ${convertType(column[2])}${if (notNull) "" else "?"},"
    }

    private fun getMetaColumnDef(className: String, column: List<String>): String {
        val code = StringBuilder()
        when (column[1]) {
            "updated_date" -> code.appendLine("@LastModifiedDate")
            "updated_user_id" -> code.appendLine("@LastModifiedBy")
            "created_date" -> code.appendLine("@CreatedDate")
            "created_user_id" -> code.appendLine("@CreatedBy")
        }
        code.appendLine(getColumn(className, column)).appendLine("lateinit ${getField(column)}".replace(",", ""))

        return code.toString()
    }

    private fun getPKColumn(column: List<String>) =
        "var ${column[1].snakeToLowerCamelCase()}: ${convertType(column[2])},"

    private fun getCompositePKConstructor(column: List<List<String>>) =
        "constructor() : this(${column.joinToString(", ") { convertToConstructorParam(it[2]) }})"

    private fun convertToConstructorParam(dbType: String): String {
        return when (dbType.uppercase()) {
            "UUID" -> "UUID.randomUUID()"
            "JSONB" -> "\"\""
            "BOOL" -> "false"
            "TIMESTAMP" -> "LocalDateTime.now()"
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
        if (args == null) {
            println("Generate NOTHING")
            return
        }
        if (!preCheck(args)) return
        output()
    }

    override fun generate(dto: RestDto): File {
        if (dto !is EntityCodeGenerateRequestDto) throw Exception("ERR_UNKNOWN")

        // [0] -> PK or UK or empty
        // [1] -> name
        // [2] -> type
        // [3] -> null or nonnull
        // [4] -> description
        val columns = dto.columns.map {
            listOf(
                it.key.trim(),
                it.name.trim(),
                it.type.trim(),
                it.nullable.trim(),
                it.description.trim()
            )
        }

        enversAudit = dto.enversAudit
        content[dto.tableName] = columns
        uniqueColumnCount[dto.tableName] = columns.filter { it[0].equals("UK", true) }.size


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

    override fun generate(tableName: String, text: String): File {
        // [0] -> PK or UK or empty
        // [1] -> name
        // [2] -> type
        // [3] -> null or nonnull
        // [4] -> description
        val contentArg = text.split("\\r?\\n").map { line -> line.split(delimiter).map { it.trim() } }
        content[tableName] = contentArg
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
            content[thisClassName] = file.readLines().map { line -> line.split(delimiter).map { it.trim() } }
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
        enversAudit = enversAuditArg?.firstOrNull()?.toBoolean() ?: false
        return true
    }

}



