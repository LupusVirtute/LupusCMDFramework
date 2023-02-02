package org.lupus.commands.core.scanner.modifiers.clazz

import org.lupus.commands.core.annotations.clazz.Continuous
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandFlag
import org.lupus.commands.core.scanner.modifiers.ClazzModifier

object ContinuousMod : ClazzModifier(Continuous::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Class<out Any>) {
        isThisAnnotationValid(annotation)
        cmdBuilder.flags.add(CommandFlag.CONTINUOUS)
    }
}
