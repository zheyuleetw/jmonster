package org.jmoster.runner

import org.jmoster.generator.CodeGenerator
import org.jmoster.generator.EntityCodeGenerator
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CodeGenerationRunner : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        if (args == null) {
            println("Generate NOTHING")
            return
        }

        args.getOptionValues("type")?.firstOrNull()?.let {
            val generator: CodeGenerator? = when (it) {
                "entity" -> EntityCodeGenerator()
                else -> null
            }

            generator?.generate(args)

        } ?: run {
            println("Please specify type")
            return
        }

    }

}

