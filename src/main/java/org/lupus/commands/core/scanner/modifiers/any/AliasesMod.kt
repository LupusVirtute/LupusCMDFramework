package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.Aliases
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object AliasesMod : AnyModifier(Aliases::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationInValid(annotation)

        annotation as Aliases
        val aliases = annotation.aliases.split(annotation.delimeter)
        cmdBuilder.aliases.addAll(aliases)
    }
}
