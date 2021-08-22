package org.lupus.commands.core.arguments

import org.lupus.commands.core.arguments.types.*

object ArgumentTypeList {
	private val arguments: MutableList<ArgumentType> = mutableListOf(
		DoubleType,

		IntegerType,
		UIntegerType,

		StringType,

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

	fun register(type: ArgumentType) {
		arguments.add(type)
	}
}