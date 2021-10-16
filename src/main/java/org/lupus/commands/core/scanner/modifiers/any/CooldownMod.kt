package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.method.Cooldown
import org.lupus.commands.core.conditions.CooldownCondition
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier
import org.lupus.commands.core.utils.TimeUtil

object CooldownMod : AnyModifier(Cooldown::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)
        annotation as Cooldown
        val time = TimeUtil.stringTimeToSeconds(annotation.time)
        cmdBuilder.addConditions(
            mutableListOf(
                CooldownCondition(time)
            )
        )
    }
}