package org.lupus.commands.core.utils

object StringUtil {
	fun listContainsStringStartingWith(list: List<String>, subString: String) : Boolean {
		for (s in list) {
			if (s.startsWith(subString))
				return true
		}
		return false
	}
}