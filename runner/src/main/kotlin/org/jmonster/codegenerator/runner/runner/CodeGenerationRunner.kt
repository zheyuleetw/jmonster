package org.jmonster.codegenerator.runner.runner

import org.jmonster.codegenerator.common.generator.EntityCodeGenerator
import org.jmonster.codegenerator.common.monster.EntityCodeCLIMonster
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CodeGenerationRunner(
    private val entityCodeGenerator: EntityCodeGenerator
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        if (args == null || args.optionNames.isNullOrEmpty()) {
            println("Generate NOTHING")
            return
        }

        args.getOptionValues("type").firstOrNull()?.let {
            when (it) {
                "entity" -> entityCodeGenerator.generate(EntityCodeCLIMonster(args))
                else -> null
            }

        } ?: run {
            println("Please specify type")
            return
        }

    }

}

