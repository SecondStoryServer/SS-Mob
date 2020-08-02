package me.syari.ss.mob.data

import me.syari.ss.mob.data.event.MobSkillEvent
import me.syari.ss.mob.loader.statement.RunnableGroup

data class MobData(
    val id: String,
    val mobSkillEvents: Map<MobSkillEvent, RunnableGroup>
)