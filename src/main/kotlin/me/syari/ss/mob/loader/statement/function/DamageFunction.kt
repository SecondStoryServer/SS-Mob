package me.syari.ss.mob.loader.statement.function

import me.syari.ss.mob.loader.error.ToRunnableResult
import me.syari.ss.mob.loader.statement.FunctionStatement
import me.syari.ss.mob.loader.statement.TargetType

object DamageFunction: FunctionStatement {
    override val label = "damage"

    override fun toRunnable(
        args: List<String>,
        targetType: TargetType?
    ): ToRunnableResult {
        val amount = args.getOrNull(0)?.toDoubleOrNull() ?: return ToRunnableResult.Error("arg0: ダメージ量の取得に失敗")
        val elementType = args.getOrNull(1) /* .toElementType */ ?: return ToRunnableResult.Error("arg1: ダメージ属性の取得に失敗")
        if (targetType == null) return ToRunnableResult.Error("target: ターゲットの取得に失敗")
        return ToRunnableResult.Success { caller ->
            targetType.get(caller).forEach { _ ->
                // TODO 攻撃処理
            }
        }
    }
}