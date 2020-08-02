package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.LivingMobData

class RunnableGroup {
    private val content = mutableListOf<(caller: LivingMobData) -> Unit>()

    fun addContent(run: (caller: LivingMobData) -> Unit) {
        content.add(run)
    }

    operator fun invoke(caller: LivingMobData) {
        content.forEach { it.invoke(caller) }
    }
}