package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.LivingMobData
import me.syari.ss.mob.data.event.MobSkillEvent

open class RunnableGroup {
    private val content = mutableListOf<RunnableGroup>()

    fun addStatement(statement: (LivingMobData) -> Unit) {
        content.add(Statement(statement))
    }

    fun addSubGroup(
        parentGroup: SubGroup?
    ): SubGroup {
        return SubGroup(parentGroup).apply { content.add(this) }
    }

    fun addEvent(
        parentGroup: SubGroup?,
        eventType: MobSkillEvent
    ): SubGroup {
        return Event(parentGroup, eventType).apply { content.add(this) }
    }

    fun get() = content.toList()

    operator fun invoke(caller: LivingMobData) {
        content.forEach { it.invoke(caller) }
    }

    class Statement(
        val statement: (LivingMobData) -> Unit
    ): RunnableGroup()

    open class SubGroup(
        val parentGroup: SubGroup?
    ): RunnableGroup()

    class Event(
        parentGroup: SubGroup?,
        val eventType: MobSkillEvent
    ): SubGroup(parentGroup)
}