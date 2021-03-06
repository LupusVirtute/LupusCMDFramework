package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.Async
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.CommandFlag
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object AsyncMod : AnyModifier(Async::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        cmdBuilder.flags.add(CommandFlag.ASYNC)
    }

}