package org.lupus.commands.core.scanner.modifiers

import org.lupus.commands.core.data.CommandBuilder

abstract class BaseModifier<T>(val annotation: Class<out Annotation>)  {
    abstract fun modify(cmdBuilder: CommandBuilder,annotation: Annotation, objModified: T)
    fun isThisAnnotationInValid(annotation: Annotation): Boolean {
        if (annotation::class.java.isAssignableFrom(this.annotation)) {
           	return false
        }
        return true
    }
}
