package org.lupus.commands.core.scanner

import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.general.NoPerm
import org.lupus.commands.core.annotations.general.Perm
import org.lupus.commands.core.annotations.method.Default
import org.lupus.commands.core.annotations.method.NotCMD
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.*
import org.lupus.commands.core.scanner.modifiers.method.DefaultMod
import java.lang.reflect.Method
import org.lupus.commands.core.utils.LogUtil.outMsg
import java.lang.reflect.Modifier
import java.util.logging.Level

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
		if(method.name.contains("\$lambda") || method.name.contains("\$default"))
			return null
		if(method.parameterCount == 0) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to not have executor parameter at least aborting..")
			return null
		}
		if(Modifier.isPrivate(method.modifiers)) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to be private aborting...")
			return null
		}

		val commandName = method.name
		val cmdBuilder = CommandBuilder(plugin, commandName, packageName, method.declaringClass)

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
		if(cmdBuilder.noCMD)
			return null

		return cmdBuilder
    }
	fun <T> modify(cmdBuilder: CommandBuilder, modifiers: List<BaseModifier<T>>) {
		for (modifier in modifiers) {
			val ann = method.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(cmdBuilder, ann, method as T)
		}
	}
}
