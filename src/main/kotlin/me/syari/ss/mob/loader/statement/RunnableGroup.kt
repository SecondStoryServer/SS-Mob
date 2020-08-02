package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.LivingMobData
import me.syari.ss.mob.data.event.MobSkillEvent

sealed class RunnableGroup {
    private val content = mutableListOf<RunnableGroup>()

    fun addStatement(statement: (LivingMobData) -> Unit) {
        content.add(Statement(statement))
    }

    fun addSubGroup(
        parentGroup: SubGroup?,
        eventType: MobSkillEvent?
    ): SubGroup {
        return SubGroup(parentGroup, eventType).apply { content.add(this) }
    }

    fun get() = content.toList()

    operator fun invoke(caller: LivingMobData) {
        content.forEach { it.invoke(caller) }
    }

    data class Statement(val statement: (LivingMobData) -> Unit): RunnableGroup()
    data class SubGroup(
        val parentGroup: SubGroup?,
        val eventType: MobSkillEvent?
    ): RunnableGroup()
}