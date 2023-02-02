package org.lupus.commands.core.scanner.modifiers.fields

import org.lupus.commands.core.annotations.Dependency
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.managers.DependencyInjectorMap
import org.lupus.commands.core.scanner.modifiers.FieldsModifier
import org.lupus.commands.core.utils.LogUtil.outMsg
import java.lang.reflect.Field

object DependencyModifier : FieldsModifier(Dependency::class.java) {
	override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Field) {
		if (!isThisAnnotationInValid(annotation)) {
			return
		}
		val type = objModified.type
		val injectedValue = DependencyInjectorMap[cmdBuilder.plugin]!![type]

		if(injectedValue == null) {
			outMsg("Error: Value that was about to get injected wasn't defined")
			return
		}

		cmdBuilder.injectableDependencies[type] = injectedValue
	}
}
