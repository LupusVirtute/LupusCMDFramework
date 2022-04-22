package org.lupus.commands.core.messages

data class KeyValueBinder(val key: String,val value: String) {
    fun get(): List<String> {
        return listOf(key, value)
    }
}