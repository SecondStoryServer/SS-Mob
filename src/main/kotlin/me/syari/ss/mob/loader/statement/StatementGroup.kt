package me.syari.ss.mob.loader.statement

sealed class StatementGroup {
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

    fun get() = content.toList()

    data class Statement(val statement: String) : StatementGroup()
    data class SubGroup(
        val parentGroup: SubGroup?,
        val statement: String
    ) : StatementGroup()
}