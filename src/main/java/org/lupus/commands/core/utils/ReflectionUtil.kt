package org.lupus.commands.core.utils

object ReflectionUtil {
	fun getPrivateField(obj: Any, string: String): Any {
		val clazz = obj::class.java
		val field = clazz.getDeclaredField(string)
		field.isAccessible = true
		val res = field.get(obj)
		field.isAccessible = false
		return res
	}
}