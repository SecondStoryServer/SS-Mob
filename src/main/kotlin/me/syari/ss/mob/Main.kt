package me.syari.ss.mob

import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        internal lateinit var mobPlugin: JavaPlugin
    }

    override fun onEnable() {
        mobPlugin = this
    }
}