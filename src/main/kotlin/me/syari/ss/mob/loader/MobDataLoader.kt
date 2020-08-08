package me.syari.ss.mob.loader

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.message.Message.send
import me.syari.ss.mob.Main.Companion.mobPlugin
import me.syari.ss.mob.data.MobData
import me.syari.ss.mob.data.event.MobSkillEvent
import me.syari.ss.mob.loader.statement.RunnableGroup
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
            val mobSkillEvents = mutableMapOf<MobSkillEvent, RunnableGroup.Event>()
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
                                if (depth == 1) {
                                    val mobSkillEvent = MobSkillEvent.Type.matchFirst(withoutColonStatement)
                                    if (mobSkillEvent == null) {
                                        errorList.add("[Skill] イベントではありません '$withoutColonStatement'")
                                        isIgnoreEvent = true
                                    } else if (!isIgnoreEvent) {
                                        currentGroup = currentGroup.addEvent(
                                            currentGroup,
                                            withoutColonStatement,
                                            mobSkillEvent
                                        )
                                        lastDepth++
                                    }
                                } else if (!isIgnoreEvent) {
                                    if (currentGroup !is StatementGroup.When) {
                                        val splitStatement = withoutColonStatement.split("\\s+".toRegex())
                                        when (splitStatement.first().toLowerCase()) {
                                            "loop" -> {
                                                if (splitStatement.size == 3) {
                                                    val period = splitStatement[1].toIntOrNull()
                                                    val times = splitStatement[2].toIntOrNull()
                                                    if (period != null && times != null) {
                                                        currentGroup = currentGroup.addLoop(
                                                            currentGroup,
                                                            withoutColonStatement,
                                                            period,
                                                            times
                                                        )
                                                    } else {
                                                        errorList.add("[Skill] パラメータは {period:int} {times:int} です '$withoutColonStatement'")
                                                    }
                                                } else {
                                                    errorList.add("[Skill] ループブロックに必要なパラメータは２つです '$withoutColonStatement'")
                                                }
                                            }
                                            "when" -> {
                                                currentGroup = currentGroup.addWhen(
                                                    currentGroup,
                                                    withoutColonStatement
                                                )
                                            }
                                            "async" -> {
                                                currentGroup = currentGroup.addAsync(
                                                    currentGroup,
                                                    withoutColonStatement
                                                )
                                            }
                                            else -> {
                                                errorList.add("[Skill] 有効なサブブロックではありません '$withoutColonStatement'")
                                            }
                                        }
                                    } else {
                                        currentGroup = currentGroup.addCondition(
                                            currentGroup,
                                            withoutColonStatement
                                        )
                                    }
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
                        parentGroup: RunnableGroup.SubGroup,
                        statementGroup: StatementGroup,
                        depth: Int
                    ) {
                        statementGroup.get().forEach {
                            for (i in 0 until depth) {
                                append("\t")
                            }
                            when (it) {
                                is StatementGroup.Statement -> {
                                    val runnable = it.statement.asFunction?.toRunnable(it.statement)
                                    val result = when (runnable) {
                                        is ToRunnableResult.Success -> {
                                            parentGroup.addStatement(runnable.run)
                                            "success"
                                        }
                                        is ToRunnableResult.Error -> runnable.error
                                        else -> "null"
                                    }
                                    appendln("${it.statement} (statement $result)")
                                }
                                is StatementGroup.Event -> {
                                    appendln("${it.statement} (event)")
                                    groupToString(parentGroup.addEvent(parentGroup, it.eventType), it, depth + 1)
                                }
                                is StatementGroup.Loop -> {
                                    appendln("${it.statement} (loop ${it.period}, ${it.times})")
                                    groupToString(parentGroup.addSubGroup(parentGroup), it, depth + 1)
                                }
                                is StatementGroup.When -> {
                                    appendln("${it.statement} (when)")
                                    groupToString(parentGroup.addSubGroup(parentGroup), it, depth + 1)
                                }
                                is StatementGroup.Condition -> {
                                    appendln("${it.statement} (condition)")
                                    groupToString(parentGroup.addSubGroup(parentGroup), it, depth + 1)
                                }
                                is StatementGroup.Async -> {
                                    appendln("${it.statement} (async)")
                                    groupToString(parentGroup.addSubGroup(parentGroup), it, depth + 1)
                                }
                            }
                        }
                    }

                    RunnableGroup.SubGroup(null).apply {
                        groupToString(this, statementGroup, 0)
                    }.get().forEach {
                        if (it is RunnableGroup.Event) {
                            mobSkillEvents[it.eventType] = it
                        }
                    }

                    appendln("Events: ${mobSkillEvents.size}")
                    mobSkillEvents.values.first().let {
                        appendln("Event[${it.eventType}]: ${it.get().size}")
                    }
                    mobSkillEvents.values.last().let {
                        appendln("Event[${it.eventType}]: ${it.get().size}")
                    }
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
                LoadedMob.Result.Success(MobData(id, mobSkillEvents))
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