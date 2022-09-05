package org.lupus.commands.core.arguments.types

import org.bukkit.command.CommandSender
import org.lupus.commands.core.arguments.ArgumentType
import org.lupus.commands.core.utils.StringUtil
import org.lupus.commands.core.utils.TimeUtil
import java.lang.StringBuilder
import java.time.Instant

object InstantType : ArgumentType(Instant::class.java,-1) {
    override fun conversion(sender: CommandSender, vararg input: String): Any? {
        val builder = StringBuilder()
        builder.append(*input)
        val processInput = builder.toString()
        return TimeUtil.stringTimeToInstant(processInput)
    }

    override fun autoComplete(sender: CommandSender, vararg input: String): MutableList<String> {
        val builder = StringBuilder()
        builder.append(input)
        val processInput = builder.toString()
        val output = "[^ ]+\$".toRegex().find(processInput)?.value ?: ""
        if (output.isEmpty())
            return mutableListOf()
        val numberRegex = "[0-9].*".toRegex()
        if (numberRegex.containsMatchIn(output)) {
            return IntegerType.autoComplete(sender, output)
        }

        return StringUtil.filterStringArrayInputStartingWithFilter(mutableListOf("mo","d","h","m","s"), output)
    }
}
