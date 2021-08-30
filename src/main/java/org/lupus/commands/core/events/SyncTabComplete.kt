package org.lupus.commands.core.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.TabCompleteEvent
import org.lupus.commands.core.managers.MainCMDs

class SyncTabComplete : Listener {
	@EventHandler
	fun onSyncTabComplete(e: TabCompleteEvent) {
		val command = Regex("/[^ ]* ").find(e.buffer, 0)?.value?.removePrefix("/")?.removeSuffix(" ") ?: return
		val buffer = e.buffer.removePrefix("/$command ")
		val commands = buffer.split(" ")
		val completions = MainCMDs[command.lowercase()]?.tabComplete(e.sender, commands) ?: return
		e.completions = completions
	}
}