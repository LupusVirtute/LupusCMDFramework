package org.lupus.commands.core.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.messages.I18nMessage
import org.lupus.commands.core.messages.KeyValueBinder

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

	fun isThatI18nSyntax(input: String) : Boolean {
		return input.startsWith("<" ) && input.endsWith(">")
	}

	fun getI18nSyntax(plugin: JavaPlugin?, input: Collection<String>): I18nMessage {
		if(input.isEmpty())
			throw IllegalArgumentException("I18n syntax is empty")

		if (!isThatI18nSyntax(input.first()))
			throw IllegalArgumentException("I18n syntax must start with < and end with >")

		val i18nTag = input.first().replace("<([^>]*)>".toRegex(),"$1")
		val mutableInput = input.toMutableList()

		mutableInput.removeFirst()

		if(mutableInput.size % 2 != 0)
			return I18nMessage(plugin, i18nTag)

		var lastKey = ""
		val keyValueList = mutableListOf<KeyValueBinder>()

		for ((idx, el) in mutableInput.withIndex()) {
			if(idx % 2 == 0)
				lastKey = el
			else
				keyValueList.add(KeyValueBinder(lastKey, el))
		}

		return I18nMessage(plugin, i18nTag, *keyValueList.toTypedArray())
	}

	fun processI18n(plugin: JavaPlugin?, response: Array<String>): Component {
		if(response.isEmpty()) {
			return Component.text("")
		}

		if(!isThatI18nSyntax(response.first())) {
			return LegacyComponentSerializer.legacySection().deserialize(response.joinToString("\n"))
		}
		return getI18nSyntax(plugin, response.toList()).getI18nResponse()
	}

}