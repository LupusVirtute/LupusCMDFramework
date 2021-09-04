package com.lupus

import org.bukkit.plugin.java.JavaPlugin
import org.lupus.commands.core.scanner.Scanner

class LCF : JavaPlugin() {
    override fun onEnable() {
        Scanner(this).scan("com.lupus.example")
    }
}