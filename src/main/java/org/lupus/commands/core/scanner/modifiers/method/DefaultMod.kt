package org.lupus.commands.core.scanner.modifiers.method

import org.lupus.commands.core.annotations.method.Default
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.MethodModifier
import java.lang.reflect.Method

object DefaultMod : MethodModifier(Default::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Method) {
        if (cmdBuilder.supCommand == null) {
            throw IllegalStateException("Command Builder doesn't have the superior command")
        }

    }
}