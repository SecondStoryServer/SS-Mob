package me.syari.ss.mob.data.event

import me.syari.ss.core.auto.Event
import me.syari.ss.mob.data.LivingMobData
import org.bukkit.entity.Mob
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent

sealed class MobSkillEvent {
    object Attack: MobSkillEvent()
    object Damaged: MobSkillEvent()
    object Clicked: MobSkillEvent()
    data class Timer(val interval: Int): MobSkillEvent()

    enum class Type(val regex: String) {
        OnAttack("onAttack"),
        OnDamaged("onDamaged"),
        OnClicked("onClicked"),
        OnTimer("onTimer-\\d+");

        companion object {
            fun matchFirst(text: String): Type? {
                return values().firstOrNull { it.regex.toRegex().matches(text) }
            }
        }
    }

    companion object: Event {
        fun from(
            eventType: Type?,
            statement: String
        ): MobSkillEvent? {
            return when (eventType) {
                Type.OnAttack -> Attack
                Type.OnDamaged -> Damaged
                Type.OnClicked -> Clicked
                Type.OnTimer -> Timer(statement.substringAfter('-').toInt())
                null -> null
            }
        }

        @EventHandler
        fun on(e: EntityDamageByEntityEvent) {
            val attacker = e.damager
            val victim = e.entity
            if (attacker is Mob) {
                LivingMobData.fromLivingEntity(attacker)?.runEvent(Attack)
            }
            if (victim is Mob) {
                LivingMobData.fromLivingEntity(victim)?.runEvent(Damaged)
            }
        }

        @EventHandler
        fun on(e: PlayerInteractAtEntityEvent) {
            val target = e.rightClicked
            if (target is Mob) {
                LivingMobData.fromLivingEntity(target)?.runEvent(Clicked)
            }
        }
    }
}