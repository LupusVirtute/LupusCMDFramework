package org.lupus.commands.core.scanner.modifiers.method

import org.lupus.commands.core.annotations.method.Filter
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.managers.FilterManager
import org.lupus.commands.core.scanner.modifiers.MethodModifier
import java.lang.reflect.Method

object FilterPassMod : MethodModifier(Filter::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Method) {
        isThisAnnotationValid(annotation)
        annotation as Filter
        val filters = annotation.filters.split(annotation.delimeter)
        val functions = filters.mapNotNull { FilterManager[it] }
        cmdBuilder.filters.addAll(functions)
    }
}
