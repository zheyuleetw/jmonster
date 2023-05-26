package org.jmonster.codegenerator.common.monster

import org.jmonster.codegenerator.common.model.dto.entity.Column
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByTableRequestDto
import org.springframework.boot.ApplicationArguments
import java.io.File

class EntityCodeCLIMonster(raw: ApplicationArguments) : Monster<Unit, ApplicationArguments?>(raw) {

    private var source: List<String> = listOf()
    private var destination: String = ""
    private var delimiter: String = "\t"
    private var packageDeclaration: String = ""
    private var enversAuditMap: MutableMap<String, Boolean> = mutableMapOf()

    override fun digestion(raw: ApplicationArguments?): Boolean {
        raw?.let {
            val sourceValid = preCheckSource(raw)
            if (!sourceValid) return false
            val destinationValid = preCheckDestination(raw)
            if (!destinationValid) return false
            preCheckDelimiter(raw)
            preCheckPackage(raw)
            preCheckEnverAudit(raw)
            return true
        } ?: run {
            println("Raw is needed to produce code")
            return false
        }

    }

    override fun produce() {
        raw?.let {
            if (!digestion(it)) return
            output()
        } ?: throw Exception("Raw is needed to produce code")
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

            val columns = file.readLines().map { line ->
                val def = line.split(delimiter)
                Column(
                    key = def[0].trim(),
                    name = def[1].trim(),
                    type = def[2].trim(),
                    nullable = def[3].trim(),
                    description = def[4].trim()
                )
            }

            EntityCodeRestMonster(
                EntityCodeGenerateByTableRequestDto(
                    tableName = thisClassName,
                    columns = columns
                )
            ).also {

                File("${thisDestination}${thisClassName}.kt").bufferedWriter().use { out ->
                    it.produce().forEach {
                        out.write(it)
                        out.newLine()
                    }
                }

                println("Generated Entity: ${thisClassName}.kt")
            }

        }
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