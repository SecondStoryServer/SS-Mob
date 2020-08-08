package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.event.MobSkillEvent

open class StatementGroup {
    private val content = mutableListOf<StatementGroup>()

    fun addStatement(statement: String) {
        content.add(Statement(statement))
    }

    fun addEvent(
        parentGroup: SubGroup?,
        statement: String,
        eventType: MobSkillEvent.Type
    ): Event {
        return Event(parentGroup, statement, MobSkillEvent.from(eventType, statement)).apply { content.add(this) }
    }

    fun addLoop(
        parentGroup: SubGroup?,
        statement: String,
        period: Int,
        times: Int
    ): Loop {
        return Loop(parentGroup, statement, period, times).apply { content.add(this) }
    }

    fun addWhen(
        parentGroup: SubGroup?,
        statement: String
    ): When {
        return When(parentGroup, statement).apply { content.add(this) }
    }

    fun addCondition(
        parentGroup: SubGroup?,
        statement: String
    ): Condition {
        return Condition(parentGroup, statement).apply { content.add(this) }
    }

    fun addAsync(
        parentGroup: SubGroup?,
        statement: String
    ): Async {
        return Async(parentGroup, statement).apply { content.add(this) }
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
        val eventType: MobSkillEvent
    ): SubGroup(parentGroup, statement)

    class Loop(
        parentGroup: SubGroup?,
        statement: String,
        val period: Int,
        val times: Int
    ): SubGroup(parentGroup, statement)

    class When(
        parentGroup: SubGroup?,
        statement: String
    ): SubGroup(parentGroup, statement)

    class Condition(
        parentGroup: SubGroup?,
        statement: String
    ): SubGroup(parentGroup, statement)

    class Async(
        parentGroup: SubGroup?,
        statement: String
    ): SubGroup(parentGroup, statement)
}