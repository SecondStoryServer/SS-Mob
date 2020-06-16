package me.syari.ss.mob.file

import me.syari.ss.mob.Main.Companion.mobPlugin
import java.io.File

class MobData private constructor(
    fileName: String,
    directory: File
) {
    private val file = File(directory, fileName)

    val id = fileName.substringBefore(FILE_EXTENSION)

    init {
        mobPlugin.logger.info(
            """
            $id
            ${file.readLines()}
        """.trimIndent()
        )
    }

    companion object {
        private const val FILE_EXTENSION = ".yml"

        fun loadMobData(directoryName: String): MutableSet<MobData> {
            var directory = mobPlugin.dataFolder
            if (!directory.exists()) directory.mkdir()
            directoryName.split("/".toRegex()).forEach { subDirectory ->
                directory = File(directory, subDirectory)
                if (!directory.exists()) directory.mkdir()
            }
            return mutableSetOf<MobData>().apply {
                directory.list()?.forEach { fileName ->
                    if (fileName.endsWith(FILE_EXTENSION)) {
                        add(MobData(fileName, directory))
                    }
                }
            }
        }
    }
}