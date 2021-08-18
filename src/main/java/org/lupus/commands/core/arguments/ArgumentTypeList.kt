package org.lupus.commands.core.arguments

import org.lupus.commands.core.arguments.types.*

object ArgumentTypeList {
	private val arguments: MutableList<ArgumentType> = mutableListOf(
		DoubleType,

		IntegerType,
		UIntegerType,

		OfflinePlayerType,
		PlayerType,

		VectorType
	)
	operator fun get(clazz: Class<out Any>): ArgumentType? {
		for (value in arguments) {
			if (value.isTheArgumentOfThisType(clazz)) {
				return value
			}
		}
		return null
	}

	fun contains(clazz: Class<*>): Boolean {
		for (value in arguments) {
			if (value.isTheArgumentOfThisType(clazz)) {
				return true;
			}
		}
		return false
	}

	fun addArgument(type: ArgumentType?) {
		if (type == null) return
		arguments.add(type)
	}
}