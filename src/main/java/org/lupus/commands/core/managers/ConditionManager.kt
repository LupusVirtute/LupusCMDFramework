package org.lupus.commands.core.managers

import org.lupus.commands.core.conditions.CreativeCond
import org.lupus.commands.core.data.ConditionFun

object ConditionManager : HashMap<String, ConditionFun>() {
	init {
		ConditionManager["creative"] = CreativeCond
	}
	override fun put(key: String, value: ConditionFun): ConditionFun? {
		val betterKey = key.lowercase()
		return super.put(betterKey, value)
	}
}