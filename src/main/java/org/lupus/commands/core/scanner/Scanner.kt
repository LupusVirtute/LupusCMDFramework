package org.lupus.commands.core.scanner

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.annotations.NotCMD
import org.lupus.commands.core.arguments.ArgumentTypeList
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.logging.Level

object Scanner {
	fun scan(plugin: JavaPlugin, packageName: String?) {
		val clazz = plugin::class.java

		outMsg("[LCF] Reflections scan started")
		var timing = System.currentTimeMillis()
		val reflections: Reflections
		reflections = Reflections(
			ConfigurationBuilder()
				.setScanners(
					SubTypesScanner(false), ResourcesScanner(), TypeAnnotationsScanner()
				)
				.setUrls(
					ClasspathHelper.forClassLoader(
						clazz.classLoader
					)
				)
		)
		outMsg("[LCF] Reflections scan ended")
		outMsg("   Time Taken = ${System.currentTimeMillis() - timing}")

		outMsg("[LCF] Performing scan for plugin ${clazz.simpleName}")
		timing = System.currentTimeMillis()
		val res = reflections.allTypes.filter {
			val pattern =  Regex("$packageName.*(Command|CMD)")
			it.contains(pattern)
		}
		println()
		println()
		println()
		outMsg("[LCF] Stopped scanning classes found ${res.size} applicable classes")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
		outMsg("[LCF] Starting to scan classes...")
		timing = System.currentTimeMillis()
		for (re in res) {
			outMsg("[LCF] Scanning $re")
			val secondClazz = clazz.classLoader.loadClass(re)
			scanClass(secondClazz)
			outMsg("[LCF] Scan ended successfully")
		}
		println()
		println()
		outMsg("[LCF] Stopped scanning classes successfully scanned all classes")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
	}

	private fun scanClass(clazz: Class<*>) {
		val simpleName = clazz.simpleName
		val commandName = simpleName.split(Regex("Command|CMD"))[0]
		outMsg("[LCF] Found sup command name = $commandName")

		if (commandName == "") {
			outMsg("[LCF] Aborting command registration due to invalid naming schema")
			return
		}

		val timing = System.currentTimeMillis()
		outMsg("[LCF] Started scanning commands inside /$commandName")
		for (declaredMethod in clazz.declaredMethods) {
			outMsg("[LCF] Scanning declared Method ${declaredMethod.name}")

			if (declaredMethod.isAnnotationPresent(NotCMD::class.java)) {
				outMsg("[LCF] Method ${declaredMethod.name} isn't command aborting...")
				continue
			}
			scanMethod(declaredMethod)

			outMsg("[LCF] Successfully scanned method ${declaredMethod.name}")
		}
		outMsg("[LCF] Stopped scanning commands successfully scanned all commands")
		outMsg("   Time taken = ${System.currentTimeMillis() - timing}")
	}
	private fun scanMethod(method: Method) {

		val commandName = method.name
		val commandArgs = method.parameters
		var first = true
		for (commandArg in commandArgs) {
			if (!ArgumentTypeList.contains(commandArg.type)) {
				outMsg("[LCF] ERROR: Command argument isn't defined in ArgumentTypeList did you load your command arguments before scanning class?", Level.SEVERE)
				outMsg("If not use @NotCMD", Level.SEVERE)
				return
			}
			if (first) {
				first = !first
				if (!CommandSender::class.java.isAssignableFrom(commandArg.type)) {
					outMsg("[LCF] First argument of method ${method.name} is not Bukkit CommandSender aborting")
					return
				}
			}
		}

	}
	private fun outMsg(string: String, level: Level) {
		Bukkit.getLogger().log(level, string)
	}
	private fun outMsg(string: String) {
		outMsg(string, Level.INFO)
	}

}