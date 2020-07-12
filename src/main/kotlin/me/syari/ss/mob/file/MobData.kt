package me.syari.ss.mob.file

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.mob.Main.Companion.mobPlugin
import java.io.File
import java.io.StringReader

class MobData private constructor(
    fileName: String,
    directory: File
) {
    private val file = File(directory, fileName)

    val id = fileName.substringBefore(FILE_EXTENSION)

    init {
        val (configContent, skillContent) = file.readText().split("skill:").let { it.getOrNull(0) to it.getOrNull(1) }
        val config = configContent?.let {
            config(mobPlugin, console, fileName, StringReader(configContent))
        }
        val skillLines = skillContent?.let { content ->
            val lines = content.withoutComment.lines().filter(String::isNotBlank).map { it to it.indentWidth }
            lines
        }
        mobPlugin.logger.info(
            """
            $id
            ${config?.section("")}
            $skillLines
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

        private val String.withoutComment get() = replace("#.*".toRegex(), "")

        private val String.indentWidth get() = indexOfFirst { !it.isWhitespace() }
    }
}