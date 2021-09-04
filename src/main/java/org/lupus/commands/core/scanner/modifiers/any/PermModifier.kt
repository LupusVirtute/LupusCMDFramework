package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.Perm
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier
import java.lang.StringBuilder

object PermModifier : AnyModifier(Perm::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)

        annotation as Perm

        val perm = StringBuilder()
        if (cmdBuilder.supCommand != null) {
            perm.append(cmdBuilder.supCommand!!.permission + ".")
        }
        perm.append(annotation.permission)
    }

}