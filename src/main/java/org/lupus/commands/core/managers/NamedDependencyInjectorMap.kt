package org.lupus.commands.core.managers

import org.bukkit.plugin.java.JavaPlugin

object NamedDependencyInjectorMap : HashMap<JavaPlugin, HashMap<String, Any>>() {
	fun addDependency(plugin: JavaPlugin, name: String, obj: Any) {
		if(this[plugin] == null)
			this[plugin] = hashMapOf()
		this[plugin]!![name] = obj
	}
	fun addDependencies(plugin: JavaPlugin, dependencies: HashMap<String, Any>) {
		if (this[plugin] == null)
			this[plugin] = dependencies
		else
			this[plugin]!!.putAll(dependencies)
	}
}
