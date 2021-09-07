package org.lupus.commands.core.scanner.modifiers.method

import org.lupus.commands.core.annotations.method.NotCMD
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.MethodModifier
import java.lang.reflect.Method

object NotCMDMod : MethodModifier(NotCMD::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Method) {
        cmdBuilder.noCMD = true
    }
}