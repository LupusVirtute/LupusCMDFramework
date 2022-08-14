package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType

object BooleanType : ArgumentType(Boolean::class.java) {
    override fun conversion(sender: CommandSender, vararg input: String): Any {
        val first = input.first().lowercase()
        return first == "yes" || first == "true"
    }

    val booleanArrayValues = arrayOf("yes","true","false","no")

    override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
        val first = input.first().lowercase()

        return booleanArrayValues.filter { it.startsWith(first) }.toMutableList()
    }
}