package org.lupus.commands.core.scanner

import org.apache.logging.log4j.Level
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.method.Default
import org.lupus.commands.core.data.CommandBuilder
import org.lupus.commands.core.scanner.modifiers.*
import org.lupus.commands.core.utils.LogUtil.outMsg
import java.lang.reflect.Modifier

class ClazzScanner(
	private val plugin: JavaPlugin,
	private val packageName: String,
	private val modifiers: List<ClazzModifier> = DefaultModifiers.clazzMods,
	private val anyModifiers: List<AnyModifier> = DefaultModifiers.anyMods,
	private val methodModifiers: List<MethodModifier> = DefaultModifiers.methodMods,
	private val paramModifiers: List<ParameterModifier> = DefaultModifiers.paramModifiers,
	private val fieldModifiers: List<FieldsModifier> = DefaultModifiers.fieldsModifier,
	private val namingSchema: Regex = Regex("Command|CMD"),
	private val permissionPrefix: String = ""
) {
	fun scan(clazz: Class<*>, sub: Boolean = false): CommandBuilder? {
		if (isClazzSubCommand(clazz) && !sub) {
			outMsg("[LCF] Command was found to be sub command without being marked as such aborting", Level.FATAL)
			return null
		}

		if (Modifier.isPrivate(clazz.modifiers)) {
			outMsg("[LCF] Command was found to be private aborting...")
			return null
		}

		val simpleName = clazz.simpleName
		if (simpleName == "Companion")
			return null
		val commandName = simpleName.split(namingSchema)[0].lowercase()

		if (!sub)
			outMsg("[LCF] Found sup command name = $commandName")

		if (commandName == "") {
			outMsg("[LCF] Aborting command registration due to invalid naming schema")
			return null
		}

		val cmdBuilder = CommandBuilder(plugin, commandName, packageName, clazz)
		cmdBuilder.paramModifiers = paramModifiers
		cmdBuilder.anyModifiers = anyModifiers
		cmdBuilder.fieldsModifiers = fieldModifiers

		cmdBuilder.permission = permissionPrefix + cmdBuilder.permission

		modify(clazz, cmdBuilder, modifiers)
		modify(clazz, cmdBuilder, anyModifiers)

		val classes = clazz.declaredClasses
		for (declaredClazz in classes) {
			val subCommand = this.scan(declaredClazz, true) ?: continue
			cmdBuilder.subCommands.add(subCommand)
		}


		for (method in clazz.declaredMethods) {

			val scanner = MethodScanner(
				method,
				plugin,
				packageName,
				cmdBuilder,
				anyModifiers,
				methodModifiers,
				paramModifiers,
				fieldModifiers,
				permissionPrefix
			)

			val command = scanner.scan() ?: continue
			command.namingSchema = namingSchema
			if (command.method!!.isAnnotationPresent(Default::class.java)) {
				cmdBuilder.method = command.method
				continue
			}
			cmdBuilder.subCommands.add(command)
		}
		cmdBuilder.runFieldScan()

		outMsg("[LCF] Main Command Built!")
		return cmdBuilder
	}

	fun <T> modify(clazz: Class<*>, cmdBuilder: CommandBuilder, modifiers: List<BaseModifier<T>>) {
		for (modifier in modifiers) {
			val ann = clazz.getAnnotation(modifier.annotation) ?: continue
			modifier.modify(cmdBuilder, ann, clazz as T)
		}
	}

	companion object {
		fun isClazzSubCommand(clazz: Class<*>): Boolean {
			try {
				clazz.getDeclaredConstructor()
			} catch (ex: Exception) {
				return true
			}
			return false
		}
	}
}
