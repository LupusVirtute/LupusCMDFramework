package org.lupus.commands.core.listeners

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.lupus.commands.core.managers.MainCMDs

class AsyncTabComplete : Listener {
	@EventHandler
	fun onAsyncTabComplete(e: AsyncTabCompleteEvent) {
		val command = Regex("/[^ ]* ").find(e.buffer, 0)?.value?.removePrefix("/")?.removeSuffix(" ") ?: return
		val buffer = e.buffer.removePrefix("/$command ")
		val commands = buffer.split(" ")
		val completions = MainCMDs[command.lowercase()]?.tabComplete(e.sender, commands) ?: return
		e.completions = completions
	}
}