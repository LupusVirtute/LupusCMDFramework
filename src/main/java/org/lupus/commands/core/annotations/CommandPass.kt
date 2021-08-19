package org.lupus.commands.core.annotations

@Target(AnnotationTarget.FUNCTION)
annotation class CommandPass(val methodName: String)
