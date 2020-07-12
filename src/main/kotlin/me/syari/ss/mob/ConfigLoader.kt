package me.syari.ss.mob

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.mob.loader.MobDataLoader.loadMobData
import org.bukkit.command.CommandSender

object ConfigLoader: OnEnable {
    override fun onEnable() {
        loadMobData(console)
    }

    fun loadMobData(output: CommandSender) {
        loadMobData(output, "mob")
    }
}