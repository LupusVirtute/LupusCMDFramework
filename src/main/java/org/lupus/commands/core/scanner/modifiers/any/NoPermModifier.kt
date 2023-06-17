package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.NoPerm
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandFlag
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object NoPermModifier : AnyModifier(NoPerm::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        cmdBuilder.permission = ""
		cmdBuilder.flags.add(CommandFlag.NO_PERM)
    }
}
