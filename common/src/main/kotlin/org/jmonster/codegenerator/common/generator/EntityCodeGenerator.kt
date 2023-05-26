package org.jmonster.codegenerator.common.generator

import org.jmonster.codegenerator.common.monster.Monster
import org.springframework.stereotype.Service


@Service
class EntityCodeGenerator {
    fun <O, R> generate(monster: Monster<O, R>): O {
        return monster.produce()
    }

}



