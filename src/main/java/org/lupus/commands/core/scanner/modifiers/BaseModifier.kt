package org.lupus.commands.core.scanner.modifiers

import org.lupus.commands.core.data.CommandBuilder

abstract class BaseModifier<T>(val annotation: Class<out Annotation>)  {
    abstract fun modify(cmdBuilder: CommandBuilder,annotation: Annotation, objModified: T)
    fun isThisAnnotationValid(annotation: Annotation): Boolean {
        return this.annotation.isInstance(annotation)
    }
}
