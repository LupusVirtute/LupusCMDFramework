package org.lupus.commands.core.annotations.general

/**
 * Makes command execute async
 * watch out for using Bukkit api with this one please
 * I am not liable for any damages with this
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Async
