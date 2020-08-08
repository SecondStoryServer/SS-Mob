package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.event.MobSkillEvent

open class StatementGroup {
    private val content = mutableListOf<StatementGroup>()

    fun addStatement(statement: String) {
        content.add(Statement(statement))
    }

    fun addSubGroup(
        parentGroup: SubGroup?,
        statement: String
    ): SubGroup {
        return SubGroup(parentGroup, statement).apply { content.add(this) }
    }

    fun addEvent(
        parentGroup: SubGroup?,
        statement: String,
        eventType: MobSkillEvent.Type?
    ): SubGroup {
        return Event(parentGroup, statement, MobSkillEvent.from(eventType, statement)).apply { content.add(this) }
    }

    fun get() = content.toList()

    class Statement(val statement: String): StatementGroup()

    open class SubGroup(
        val parentGroup: SubGroup?,
        val statement: String
    ): StatementGroup()

    class Event(
        parentGroup: SubGroup?,
        statement: String,
        val eventType: MobSkillEvent?
    ): SubGroup(parentGroup, statement)
}