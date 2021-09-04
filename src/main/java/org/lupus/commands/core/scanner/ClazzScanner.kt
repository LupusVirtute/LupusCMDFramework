package org.lupus.commands.core.scanner

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.clazz.SubCommand
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.*
import org.lupus.commands.core.utils.LogUtil.outMsg

class ClazzScanner(
		private val clazz: Class<*>,
		private val plugin: JavaPlugin,
		private val packageName: String,
		private val modifiers: List<ClazzModifier> = DefaultModifiers.clazzMods,
		private val anyModifiers: List<AnyModifier> = DefaultModifiers.anyMods,
		private val methodModifiers: List<MethodModifier> = DefaultModifiers.methodMods,
		private val paramModifiers: List<ParameterModifier> = DefaultModifiers.paramModifiers,
		private val namingSchema: Regex = Regex("Command|CMD"),
		private val permissionPrefix: String = ""
) {
    fun scan(sub: Boolean = false): CommandBuilder? {
		if(clazz.isAnnotationPresent(SubCommand::class.java) && !sub) {
			return null
		}

		val simpleName = clazz.simpleName
		val commandName = simpleName.split(namingSchema)[0].lowercase()

		if(!sub)
			outMsg("[LCF] Found sup command name = $commandName")

		if (commandName == "") {
			outMsg("[LCF] Aborting command registration due to invalid naming schema")
			return null
		}


		val cmdBuilder = CommandBuilder(plugin, commandName, packageName, clazz)
		cmdBuilder.permission = permissionPrefix
		modify(cmdBuilder, modifiers)
		modify(cmdBuilder, anyModifiers);

		for (method in clazz.declaredMethods) {
			val scanner = MethodScanner(
				method,
				plugin,
				packageName,
				cmdBuilder,
				anyModifiers,
				methodModifiers,
				paramModifiers,
				permissionPrefix
			)
			val command = scanner.scan() ?: continue
			cmdBuilder.subCommands.add(command)
		}

		outMsg("[LCF] Main Command Built!")
		return cmdBuilder
    }

	fun <T> modify(cmdBuilder: CommandBuilder, modifiers: List<BaseModifier<T>>) {
		for (modifier in modifiers) {
			val ann = clazz.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(cmdBuilder, ann, clazz as T)
		}
	}
}
