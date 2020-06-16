package me.syari.ss.mob

import me.syari.ss.core.auto.OnEnable
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        internal lateinit var mobPlugin: JavaPlugin
    }

    override fun onEnable() {
        mobPlugin = this
        OnEnable.register(ConfigLoader)
    }
}