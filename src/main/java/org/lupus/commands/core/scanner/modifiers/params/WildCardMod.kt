package org.lupus.commands.core.scanner.modifiers.params

import org.lupus.commands.core.annotations.parameters.WildCard
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import java.lang.reflect.Parameter

object WildCardMod : ParameterModifier(WildCard::class.java) {
	override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Parameter) {
		if(!isThisAnnotationValid(annotation))
			return
		val method = cmdBuilder.method ?: return
		var indexOfParameterWildCard = -1
		for ((idx, parameter) in method.parameters.iterator().withIndex()) {
			if (parameter == objModified) {
				indexOfParameterWildCard = idx
			}
		}
		cmdBuilder.wildCards += indexOfParameterWildCard
	}
}
