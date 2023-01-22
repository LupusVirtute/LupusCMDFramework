package org.lupus.commands.core.scanner.modifiers.fields

import org.lupus.commands.core.annotations.NamedDependency
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.managers.NamedDependencyInjectorMap
import org.lupus.commands.core.scanner.modifiers.FieldsModifier
import org.lupus.commands.core.utils.LogUtil
import java.lang.reflect.Field

object NamedDependencyModifier : FieldsModifier(NamedDependency::class.java) {
	override fun modify(cmdBuilder: CommandBuilder, annotation: Annotation, objModified: Field) {
		if (!isThisAnnotationValid(annotation)) {
			return
		}
		val name = objModified.name
		val injectedValue = NamedDependencyInjectorMap[cmdBuilder.plugin]!![name]

		if(injectedValue == null) {
			LogUtil.outMsg("Error: Value that was about to get injected wasn't defined")
			return
		}

		cmdBuilder.namedInjectableDependencies[name] = injectedValue
	}
}
