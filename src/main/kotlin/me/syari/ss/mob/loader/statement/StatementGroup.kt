package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.event.MobSkillEvent

open class StatementGroup {
    private val content = mutableListOf<StatementGroup>()

    private fun <T: StatementGroup> content(group: T): T {
        content.add(group)
        return group
    }

    fun addStatement(statement: String) {
        content(Statement(statement))
    }

    fun addEvent(
        parentGroup: SubGroup?,
        statement: String,
        eventType: MobSkillEvent.Type
    ): Event {
        return content(Event(parentGroup, statement, MobSkillEvent.from(eventType, statement)))
    }

    fun addLoop(
        parentGroup: SubGroup?,
        statement: String,
        period: Int,
        times: Int
    ): Loop {
        return content(Loop(parentGroup, statement, period, times))
    }

    fun addWhen(
        parentGroup: SubGroup?,
        statement: String
    ): When {
        return content(When(parentGroup, statement))
    }

    fun addCondition(
        parentGroup: SubGroup?,
        statement: String
    ): Condition {
        return content(Condition(parentGroup, statement))
    }

    fun addAsync(
        parentGroup: SubGroup?,
        statement: String
    ): Async {
        return content(Async(parentGroup, statement))
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