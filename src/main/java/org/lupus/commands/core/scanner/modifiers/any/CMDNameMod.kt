package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.CMDName
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object CMDNameMod : AnyModifier(CMDName::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)
        annotation as CMDName
        val name = annotation.name
        cmdBuilder.name = name
    }
}
