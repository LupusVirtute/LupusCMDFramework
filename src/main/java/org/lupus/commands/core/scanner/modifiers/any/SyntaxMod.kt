package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.Syntax
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object SyntaxMod : AnyModifier(Syntax::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)
        annotation as Syntax
        cmdBuilder.syntax = StringBuilder(annotation.syntax)
    }

}