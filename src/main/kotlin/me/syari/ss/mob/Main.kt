package me.syari.ss.mob

import me.syari.ss.core.auto.Event
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.mob.data.event.MobSkillEvent
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        internal lateinit var mobPlugin: JavaPlugin
    }

    override fun onEnable() {
        mobPlugin = this
        OnEnable.register(ConfigLoader)
        Event.register(this, MobSkillEvent)
    }
}