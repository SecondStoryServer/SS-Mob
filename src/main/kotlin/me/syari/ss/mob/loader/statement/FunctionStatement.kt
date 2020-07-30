package me.syari.ss.mob.loader.statement

import me.syari.ss.mob.data.LivingMobData
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

sealed class ToRunnableResult {
    class Success(val run: (caller: LivingMobData) -> Unit): ToRunnableResult()
    class Error(val error: String): ToRunnableResult()
}