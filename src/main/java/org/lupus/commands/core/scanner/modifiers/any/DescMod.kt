package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.Desc
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object DescMod : AnyModifier(Desc::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)
        annotation as Desc
        cmdBuilder.description = annotation.desc
    }
}
