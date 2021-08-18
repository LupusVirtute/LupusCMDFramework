package org.lupus.commands.core

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.scanner.Scanner

class CommandFramework : JavaPlugin() {
	override fun onEnable() {
		Bukkit.getScheduler().runTaskLater(this, Runnable {
			Scanner(this).scan("org.lupus.commands.core.commands")
		},1L)
		super.onEnable()
	}
}