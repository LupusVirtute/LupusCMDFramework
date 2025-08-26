package org.lupus.commands.core.scanner

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.*
import org.lupus.commands.core.utils.LogUtil.outMsg
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class MethodScanner(
	val method: Method,
	val plugin: JavaPlugin,
	val packageName: String,
	val supCommand: CommandBuilder,
	val anyModifiers: List<AnyModifier> = DefaultModifiers.anyMods,
	val methodModifiers: List<MethodModifier> = DefaultModifiers.methodMods,
	val paramModifiers: List<ParameterModifier> = DefaultModifiers.paramModifiers,
	val fieldModifiers: List<FieldsModifier> = DefaultModifiers.fieldsModifier,
	val permissionPrefix: String = "",
) {

	fun scan(): CommandBuilder? {
		if (Modifier.isPrivate(method.modifiers)) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to be private aborting...")
			return null
		}
		if (method.isBridge || method.isSynthetic) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to be compiler generated aborting...")
			return null
		}
		if (method.name.lowercase().contains("whenmappings")) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to be kotlin when mappings aborting...")
			return null
		}
		if (method.name.contains("\$lambda") || method.name.contains("\$default")) {
			outMsg("[LCF] INFO: Command ${method.name} was found to be lambda functions kotlin compiled aborting...")
			return null
		}
		if (method.parameterCount == 0) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to not have executor parameter at least aborting..")
			return null
		}

		val commandName = method.name
		val cmdBuilder = CommandBuilder(plugin, commandName, packageName, method.declaringClass, supCommand)

		cmdBuilder.anyModifiers = anyModifiers
		cmdBuilder.paramModifiers = paramModifiers
		cmdBuilder.fieldsModifiers = fieldModifiers

		cmdBuilder.method = method
		cmdBuilder.permission = supCommand.permission
		cmdBuilder.supCommand = supCommand
		if (cmdBuilder.method == null) {
			return null
		}

		modify(cmdBuilder, anyModifiers)
		modify(cmdBuilder, methodModifiers)
		if (cmdBuilder.noCMD)
			return null

		return cmdBuilder
	}

	fun <T> modify(cmdBuilder: CommandBuilder, modifiers: List<BaseModifier<T>>) {
		for (modifier in modifiers) {
			val annotations = method.getAnnotationsByType(modifier.annotation) ?: continue
			for (annotation in annotations) {
				modifier.modify(cmdBuilder, annotation, method as T)
			}
		}
	}
}
