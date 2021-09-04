package org.lupus.commands.core.scanner.modifiers.any

import org.lupus.commands.core.annotations.general.Conditions
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.data.ConditionFun
import org.lupus.commands.core.managers.ConditionManager
import org.lupus.commands.core.scanner.modifiers.AnyModifier

object ConditionsMod : AnyModifier(Conditions::class.java) {
    override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Any) {
        isThisAnnotationValid(annotation)
        annotation as Conditions
        val conditionNames = annotation.conditions
        val names = conditionNames.split(annotation.delimeter)
        val conditions = mutableListOf<ConditionFun>()
        for (name in names) {
            conditions.add(ConditionManager[name] ?: continue)
        }
        cmdBuilder.addConditions(conditions)
    }

}