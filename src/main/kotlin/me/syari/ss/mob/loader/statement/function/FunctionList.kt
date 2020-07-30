package me.syari.ss.mob.loader.statement.function

import me.syari.ss.mob.loader.statement.FunctionStatement

private val functionMap = register(
    DamageFunction
)

private fun register(vararg functionStatement: FunctionStatement): Map<String, FunctionStatement> {
    return functionStatement.map { it.label.toLowerCase() to it }.toMap()
}

val String.asFunction
    get() = functionMap[substringBefore(' ').toLowerCase()]