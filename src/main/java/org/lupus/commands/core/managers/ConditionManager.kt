package org.lupus.commands.core.managers

import org.lupus.commands.core.conditions.CreativeCond
import org.lupus.commands.core.conditions.SurvivalCond
import org.lupus.commands.core.data.ConditionFun

object ConditionManager : HashMap<String, ConditionFun>() {
	init {
		ConditionManager["creative"] = CreativeCond
		ConditionManager["survival"] = SurvivalCond
	}

	override operator fun get(key: String): ConditionFun? {
		val betterKey = key.lowercase()
		return super.get(betterKey)
	}

	override fun put(key: String, value: ConditionFun): ConditionFun? {
		val betterKey = key.lowercase()
		return super.put(betterKey, value)
	}
}
