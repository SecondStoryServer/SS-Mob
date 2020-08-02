package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.event.MobSkillEvent

sealed class StatementGroup {
    private val content = mutableListOf<StatementGroup>()

    fun addStatement(statement: String) {
        content.add(Statement(statement))
    }

    fun addSubGroup(
        parentGroup: SubGroup?,
        statement: String,
        eventType: MobSkillEvent.Type?
    ): SubGroup {
        return SubGroup(parentGroup, statement, MobSkillEvent.from(eventType, statement)).apply { content.add(this) }
    }

    fun get() = content.toList()

    data class Statement(val statement: String): StatementGroup()
    data class SubGroup(
        val parentGroup: SubGroup?,
        val statement: String,
        val eventType: MobSkillEvent?
    ): StatementGroup()
}