package plugin.history

import mindustry.game.Team
import mindustry.type.UnitType
import mindustry.world.Block
import plugin.utils.configAsString
import plugin.utils.formatAgo

data class HistoryRecord(
    val playerName: String?, val playerId: Int?, val type: HistoryType, val block: Block,
    val unit: UnitType?, val time: Long, val center: Boolean, val team: Team, val rotation: Int,
    val configBefore: Any?, val configAfter: Any?
) {
    fun getMessage(): String {
        var actor = "[white]" + (playerName ?: unit?.emoji() ?: "unknown")
        if (playerId != null) actor = "[lightgray][$playerId] $actor"

        val ago = "[lightgray][${formatAgo(time)}] "

        return ago + when (type) {
            HistoryType.Rotate ->
                "$actor [tan]rotated [white]${block.emoji()}"

            HistoryType.BreakBlock ->
                "$actor [red]broken [white]${block.emoji()}"

            HistoryType.BuildBlock ->
                "$actor [green]built [white]${block.emoji()}"

            HistoryType.Configure -> {
                var config = "[lightgray]${configAsString(configBefore)} -> ${configAsString(configAfter)}"
                return "$actor [tan]configured [white]${block.emoji()} $config"
            }

            HistoryType.DestroyBlock ->
                "[white]${team.emoji}${block.emoji()} [red]destroyed"

            else ->
                "$actor [tan]${type.name} [white]${block.emoji()}"
        }
    }
}
