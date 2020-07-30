package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.LivingMobData
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

sealed class TargetType {
    open fun get(
        caller: LivingMobData
    ): List<Entity> {
        throw UnsupportedOperationException()
    }

    object Self: TargetType() {
        override fun get(
            caller: LivingMobData
        ): List<Entity> {
            return listOf(caller.entity)
        }
    }

    object Target: TargetType() {
        override fun get(
            caller: LivingMobData
        ): List<Entity> {
            return listOfNotNull(caller.entity.target)
        }
    }

    object Trigger: TargetType() {
        override fun get(
            caller: LivingMobData
        ): List<Entity> {
            TODO()
        }
    }

    class PlayersInRadius(private val radius: Double): TargetType() {
        constructor(parameter: Map<String, String>): this(parameter["r"]?.toDoubleOrNull() ?: 0.0)

        override fun get(
            caller: LivingMobData
        ): List<Entity> {
            return caller.entity.getNearbyEntities(radius, radius, radius).filterIsInstance(Player::class.java)
        }
    }

    companion object {
        private fun getTargetType(
            type: String,
            parameter: Map<String, String>
        ): TargetType? {
            return when (type.toLowerCase()) {
                "@self" -> Self
                "@target" -> Target
                "@trigger" -> Trigger
                "@playersinradius" -> PlayersInRadius(parameter)
                else -> null
            }
        }

        val String?.toTargetType: TargetType?
            get() {
                if (this == null) return null
                val parameterString = "(?<=\\{).*?(?=})".toRegex().find(this)?.value
                val parameter = parameterString?.split("[;,]".toRegex())?.mapNotNull {
                    val index = it.indexOf('=')
                    if (index != -1) {
                        val key = it.substring(0, index)
                        val value = it.substring(index + 1, it.length)
                        key to value
                    } else {
                        null
                    }
                }?.toMap() ?: emptyMap()
                return getTargetType(parameterString?.let { replace(it, "") } ?: this, parameter)
            }
    }
}