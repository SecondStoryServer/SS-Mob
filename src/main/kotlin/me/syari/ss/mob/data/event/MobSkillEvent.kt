package me.syari.ss.mob.data.event

enum class MobSkillEvent(val regex: String) {
    OnAttack("onAttack"),
    OnDamaged("onDamaged"),
    OnClick("onClick"),
    OnTimer("onTimer-\\d+");

    companion object {
        fun matchFirst(text: String): MobSkillEvent? {
            return values().firstOrNull { it.regex.toRegex().matches(text) }
        }
    }
}