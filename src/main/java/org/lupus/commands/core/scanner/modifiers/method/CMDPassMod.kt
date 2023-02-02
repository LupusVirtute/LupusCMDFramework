package org.lupus.commands.core.scanner.modifiers.method

import org.lupus.commands.core.annotations.method.CMDPass
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.MethodModifier
import java.lang.reflect.Method

object CMDPassMod : MethodModifier(CMDPass::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Method) {
        isThisAnnotationInValid(annotation)
        annotation as CMDPass
        val cmdPath = annotation.commandPath
        cmdBuilder.addSubCommandPass("${cmdBuilder.packageName}.$cmdPath")
    }

}
