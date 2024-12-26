package org.lupus.commands.core.scanner.modifiers.params

import org.lupus.commands.core.annotations.parameters.LimitTabOutput
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import java.lang.reflect.Parameter

object LimitTabOutputMod  : ParameterModifier(LimitTabOutput::class.java) {
	override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Parameter) {
		if(!isThisAnnotationValid(annotation)) {
			return
		}
		annotation as LimitTabOutput
		val method = cmdBuilder.method ?: return
		val tabLimit = annotation.tabLimit
		var indexOfParameterTabLimit = -1
		for ((idx, parameter) in method.parameters.iterator().withIndex()) {
			if (parameter == objModified) {
				indexOfParameterTabLimit = idx
			}
		}
		if(indexOfParameterTabLimit < 0) {
			return
		}
		cmdBuilder.limitTabOutputs[indexOfParameterTabLimit-1] = tabLimit
	}
}
