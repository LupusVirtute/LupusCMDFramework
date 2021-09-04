package org.lupus.commands.core.annotations.general

/**
 * Add alias to command
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Aliases(val aliases: String, val delimeter: String = "|")
