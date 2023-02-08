package org.lupus.commands.core.annotations.parameters

/**
 * Sets the default value for the parameter if player doesn't set it correctly
 * TODO
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Optional(val defaultValue: String)
