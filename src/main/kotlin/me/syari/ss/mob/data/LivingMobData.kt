package me.syari.ss.mob.data

import me.syari.ss.core.scheduler.CreateScheduler.runTimer
import me.syari.ss.mob.Main.Companion.mobPlugin
import me.syari.ss.mob.data.event.MobSkillEvent
import org.bukkit.entity.Mob

data class LivingMobData(
    val entity: Mob,
    val data: MobData
) {
    fun runEvent(event: MobSkillEvent) {
        data.mobSkillEvents[event]?.invoke(this)
    }

    fun runAllTimer() {
        data.mobSkillEvents.forEach {
            val timer = it.key as? MobSkillEvent.Timer ?: return@forEach
            runTimer(mobPlugin, timer.interval.toLong()) {
                if (entity.isValid) {
                    it.value.invoke(this@LivingMobData)
                } else {
                    cancel()
                }
            }
        }
    }

    companion object {
        fun fromLivingEntity(entity: Mob): LivingMobData? {
            TODO()
        }
    }
}