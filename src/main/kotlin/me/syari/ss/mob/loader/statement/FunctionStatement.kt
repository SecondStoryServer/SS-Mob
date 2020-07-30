package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.loader.error.ToRunnableResult
import me.syari.ss.mob.loader.statement.TargetType.Companion.toTargetType

interface FunctionStatement {
    val label: String

    fun toRunnable(statement: String): ToRunnableResult {
        val split = statement.split("\\s+".toRegex()).toMutableList()
        split.removeAt(0)
        val targetType = split.firstOrNull { it.startsWith('@') }.toTargetType
        return toRunnable(split.filterNot { it.startsWith('@') }, targetType)
    }

    fun toRunnable(
        args: List<String>,
        targetType: TargetType?
    ): ToRunnableResult
}