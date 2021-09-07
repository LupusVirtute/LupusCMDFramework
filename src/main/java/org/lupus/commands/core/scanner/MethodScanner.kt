package org.lupus.commands.core.scanner

import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.general.NoPerm
import org.lupus.commands.core.annotations.general.Perm
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.AnyModifier
import org.lupus.commands.core.scanner.modifiers.BaseModifier
import org.lupus.commands.core.scanner.modifiers.MethodModifier
import org.lupus.commands.core.scanner.modifiers.ParameterModifier
import java.lang.reflect.Method
import org.lupus.commands.core.utils.LogUtil.outMsg
import java.util.logging.Level

class MethodScanner(
	val method: Method,
	val plugin: JavaPlugin,
	val packageName: String,
	val supCommand: CommandBuilder,
	val anyModifiers: List<AnyModifier> = DefaultModifiers.anyMods,
	val methodModifiers: List<MethodModifier> = DefaultModifiers.methodMods,
	val paramModifiers: List<ParameterModifier> = DefaultModifiers.paramModifiers,
	val permissionPrefix: String = "",
) {

    fun scan(): CommandBuilder? {
		if(method.parameterCount == 0) {
			outMsg("[LCF] INFO: Command method ${method.name} was found to not have executor parameter at least aborting..")
			return null
		}
		val commandName = method.name
		val commandArgs = method.parameters
		var first = true
		val cmdBuilder = CommandBuilder(plugin, commandName, packageName, method.declaringClass)

		cmdBuilder.anyModifiers = anyModifiers
		cmdBuilder.paramModifiers = paramModifiers

		cmdBuilder.method = method
		cmdBuilder.permission = permissionPrefix
		cmdBuilder.supCommand = supCommand


		for (commandArg in commandArgs) {
			if (!ArgumentTypeList.contains(commandArg.type)) {
				outMsg("[LCF] ERROR: Command argument isn't defined in ArgumentTypeList did you load your command arguments before scanning class?", Level.SEVERE)
				outMsg("If not use @NotCMD", Level.SEVERE)
				return null
			}
			if (first) {
				first = !first
				if (!CommandSender::class.java.isAssignableFrom(commandArg.type)) {
					outMsg("[LCF] First argument of method ${method.name} is not Bukkit CommandSender aborting")
					return null
				}
				cmdBuilder.executorParameter = commandArg
				continue
			}
			cmdBuilder.addParameter(commandArg)
		}

		modify(cmdBuilder, anyModifiers)
		modify(cmdBuilder, methodModifiers)




		return cmdBuilder
    }
	fun <T> modify(cmdBuilder: CommandBuilder, modifiers: List<BaseModifier<T>>) {
		for (modifier in modifiers) {
			val ann = method.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(cmdBuilder, ann, method as T)
		}
	}
}