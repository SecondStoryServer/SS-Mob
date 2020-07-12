package me.syari.ss.mob.loader.error

import me.syari.ss.mob.data.MobData

sealed class LoadedMobData {
    class Success(val data: MobData): LoadedMobData()
    class Error(
        val fileName: String,
        val errorList: List<String>
    ): LoadedMobData()
}