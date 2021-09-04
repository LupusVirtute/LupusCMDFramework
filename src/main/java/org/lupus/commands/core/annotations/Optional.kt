package org.lupus.commands.core.annotations

/**
 * Sets the default value for the parameter if player doesn't set it
 * TODO
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Optional(val default: String)
