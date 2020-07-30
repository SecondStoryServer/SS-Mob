package me.syari.ss.mob.loader.error

import me.syari.ss.mob.data.LivingMobData

sealed class ToRunnableResult {
    class Success(val function: (caller: LivingMobData) -> Unit): ToRunnableResult()
    class Error(val error: String): ToRunnableResult()
}