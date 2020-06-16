package me.syari.ss.mob

import me.syari.ss.core.auto.OnEnable
import me.syari.ss.mob.file.MobData.Companion.loadMobData

object ConfigLoader: OnEnable {
    override fun onEnable() {
        loadMobData()
    }

    fun loadMobData() {
        loadMobData("mob")
    }
}