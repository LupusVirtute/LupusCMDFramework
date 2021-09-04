package org.lupus.commands.core.annotations.general

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Conditions(val conditions: String, val delimeter: String = "|")
