package org.lupus.commands.core.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Perm(val permission: String)