package org.jmonster.codegenerator.common.monster

abstract class Monster<O, Raw>(raw: Raw) {

    var raw: Raw? = raw
    abstract fun digestion(raw: Raw)
    abstract fun produce(): O
}