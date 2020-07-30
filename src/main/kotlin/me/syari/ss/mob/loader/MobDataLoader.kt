package me.syari.ss.mob.loader

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.message.Message.send
import me.syari.ss.mob.Main.Companion.mobPlugin
import me.syari.ss.mob.data.MobData
import me.syari.ss.mob.data.event.MobSkillEvent
import me.syari.ss.mob.loader.statement.StatementGroup
import me.syari.ss.mob.loader.statement.ToRunnableResult
import me.syari.ss.mob.loader.statement.function.asFunction
import org.bukkit.command.CommandSender
import java.io.File
import java.io.StringReader

object MobDataLoader {
    private const val FILE_EXTENSION = ".yml"

    fun loadMobData(
        output: CommandSender,
        directoryName: String
    ): MutableSet<MobData> {
        var directory = mobPlugin.dataFolder
        if (!directory.exists()) directory.mkdir()
        directoryName.split("/".toRegex()).forEach { subDirectory ->
            directory = File(directory, subDirectory)
            if (!directory.exists()) directory.mkdir()
        }
        return mutableSetOf<MobData>().apply {
            directory.list()?.forEach { fileName ->
                if (fileName.endsWith(FILE_EXTENSION)) {
                    loadMobData(fileName, directory).let {
                        when (it) {
                            is LoadedMob.Result.Success -> {
                                add(it.data)
                            }
                            is LoadedMob.Result.Error -> {
                                it.errorList.forEach { error ->
                                    output.send("[${it.fileName}] $error")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private inline val String.withoutComment get() = replace("#.*".toRegex(), "")

    private inline val String.withoutBlankLines get() = lines().filter(String::isNotBlank)

    private inline val List<String>.withIndentWidth get() = map { it to it.indentWidth }

    private inline val String.indentWidth get() = indexOfFirst { !it.isWhitespace() }

    private inline val String.withoutSurroundBlank get() = replace("^\\s+".toRegex(), "").replace("\\s+$".toRegex(), "")

    class LoadedMob {
        sealed class Result {
            class Success(val data: MobData): Result()
            class Error(
                val fileName: String,
                val errorList: List<String>
            ): Result()
        }

        lateinit var loadedMobData: Result
        var lastModified = 0L
    }

    private val loadedMobDataCacheList = mutableMapOf<String, LoadedMob>()

    private fun loadMobData(
        fileName: String,
        directory: File
    ): LoadedMob.Result {
        val file = File(directory, fileName)
        val fileLastModified = file.lastModified()
        val id = fileName.substringBefore(FILE_EXTENSION)
        val cache = loadedMobDataCacheList.getOrPut(id) { LoadedMob() }
        return if (cache.lastModified != fileLastModified) {
            val (configContent, skillContent) = file.readText().split("skill:").let { it.getOrNull(0) to it.getOrNull(1) }
            val config = configContent?.let {
                config(mobPlugin, console, fileName, StringReader(configContent))
            }
            val errorList = mutableListOf<String>()
            val skillLines = skillContent?.let { content ->
                val lines = content.withoutComment.withoutBlankLines.withIndentWidth
                val statementGroup = StatementGroup.SubGroup(null, "")
                val minIndentWidth = lines.firstOrNull()?.second
                if (minIndentWidth != null) {
                    var lastDepth = 1
                    var isIgnoreEvent = false
                    var currentGroup = statementGroup
                    lines.forEach { (rawStatement, indentWidth) ->
                        val statement = rawStatement.withoutSurroundBlank
                        val depth = indentWidth / minIndentWidth
                        if (depth < lastDepth) {
                            for (i in depth until lastDepth) {
                                currentGroup.parentGroup?.let {
                                    currentGroup = it
                                } ?: errorList.add("[Skill] インデントが不正です '$statement'")
                            }
                            lastDepth = depth
                        }
                        when {
                            ":\\s*\$".toRegex().find(statement) != null -> {
                                val withoutColonStatement = statement.removeSuffix(":")
                                if (depth == 1 && isIgnoreEvent) {
                                    isIgnoreEvent = false
                                }
                                if (depth == 1 && MobSkillEvent.matchFirst(withoutColonStatement) == null) {
                                    errorList.add("[Skill] イベントではありません '$withoutColonStatement'")
                                    isIgnoreEvent = true
                                } else if (!isIgnoreEvent) {
                                    currentGroup = currentGroup.addSubGroup(currentGroup, withoutColonStatement)
                                    lastDepth++
                                }
                            }
                            currentGroup.parentGroup != null -> {
                                currentGroup.addStatement(statement)
                            }
                            !isIgnoreEvent -> {
                                errorList.add("[Skill] 関数はイベント内に入れてください '$statement'")
                            }
                        }
                    }
                }

                buildString {
                    fun groupToString(
                        statementGroup: StatementGroup,
                        depth: Int
                    ) {
                        statementGroup.get().forEach { it ->
                            for (i in 0 until depth) {
                                append("\t")
                            }
                            when (it) {
                                is StatementGroup.Statement -> {
                                    val runnable = it.statement.asFunction?.toRunnable(it.statement)
                                    val result = when (runnable) {
                                        is ToRunnableResult.Success -> "success"
                                        is ToRunnableResult.Error -> runnable.error
                                        else -> "null"
                                    }
                                    appendln(it.statement + "(" + result + ")")
                                }
                                is StatementGroup.SubGroup -> {
                                    appendln(it.statement + "---")
                                    groupToString(it, depth + 1)
                                }
                            }
                        }
                    }

                    groupToString(statementGroup, 0)
                }
            }
            mobPlugin.logger.info(
                """
                |$id
                |${config?.section("")}
                |$skillLines
                """.trimMargin()
            )
            if (errorList.isEmpty()) {
                LoadedMob.Result.Success(MobData(id))
            } else {
                LoadedMob.Result.Error(fileName, errorList)
            }.apply {
                cache.loadedMobData = this
                cache.lastModified = fileLastModified
            }
        } else {
            cache.loadedMobData
        }
    }
}