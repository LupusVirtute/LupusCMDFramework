package org.lupus.commands.core.conditions

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.lupus.commands.core.data.CommandLupi
import org.lupus.commands.core.data.ConditionFun
import org.lupus.commands.core.managers.CooldownManager
import org.lupus.commands.core.messages.I18n
import org.lupus.commands.core.messages.KeyValueBinder
import org.lupus.commands.core.utils.TimeUtil
import java.time.Instant

class CooldownCondition(
    private val cooldown: Long
    ) : ConditionFun() {
    override fun run(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Boolean {
        if (sender !is Player)
            return true

		if (sender.hasPermission("cooldown.ignore"))
			return false

		val namespace = commandLupi.getNameSpace()
        val hasCooldown = !CooldownManager.playerHasCooldown(sender, namespace)
        if (hasCooldown) {
            CooldownManager.setPlayerCooldown(sender, namespace, Instant.now().plusSeconds(this.cooldown))
        }
        return hasCooldown
    }
    override fun getResponse(sender: CommandSender, commandLupi: CommandLupi, args: Array<Any>): Any {
        if (sender !is Player)
            return ""
        val namespace = commandLupi.getNameSpace()
        val instant = CooldownManager.getPlayerCooldown(sender, namespace)
        val time = TimeUtil.epochSecondToString(instant)
        val cooldownTime = Placeholder.parsed("cooldown-time", time)

        return I18n[commandLupi.pluginRegistering, "cooldown", cooldownTime]
    }

}
