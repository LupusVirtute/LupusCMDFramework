package org.lupus.commands.core.arguments

import org.lupus.commands.core.arguments.types.*

object ArgumentTypeList {
	private val arguments: MutableList<ArgumentType> = mutableListOf(
		BooleanType,

		DoubleType,

		IntegerType,
		UIntegerType,

		StringType,
		GreedyStringType,

		InstantType,

		OfflinePlayerType,
		PlayerType,
		CommandSenderType,


		VectorType,
		WorldType,
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
		return get(clazz) != null
	}

	fun register(type: ArgumentType) {
		arguments.add(type)
	}
}
