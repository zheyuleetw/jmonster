package org.jmonster.codegenerator.common.monster

abstract class Monster<OutputType, RawType>(raw: RawType) {

    protected var raw: RawType? = raw
    abstract fun digestion(raw: RawType): Boolean
    abstract fun produce(): OutputType
}