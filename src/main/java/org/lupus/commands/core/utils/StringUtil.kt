package org.lupus.commands.core.utils

object StringUtil {
	fun listContainsStringStartingWith(list: List<String>, subString: String) : Boolean {
		for (s in list) {
			if (s.startsWith(subString))
				return true
		}
		return false
	}

	/**
	 * Case insensitive filter used for tab complete actions
	 */
	fun filterStringArrayInputStartingWithFilter(input: List<String>, filter: String): MutableList<String> {
		val filteredOutput = mutableListOf<String>()
		val betterFilter = filter.lowercase()
		for (s in input) {
			if (s.lowercase().startsWith(betterFilter))
				filteredOutput.add(s)
		}
		return filteredOutput
	}
}