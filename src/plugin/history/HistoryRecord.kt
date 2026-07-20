package plugin.history

import arc.util.Log
import mindustry.content.Blocks
import mindustry.game.Team
import mindustry.type.UnitType
import mindustry.world.Block
import plugin.utils.configAsString
import plugin.utils.formatAgo

data class HistoryRecord(
    val actor: HistoryActor?,
    val type: HistoryType,
    val block: Block,
    val unit: UnitType?,
    val time: Long,
    val center: Boolean,
    val team: Team,
    val rotation: Int,
    val configAfter: Any?
) {
    fun getMessage(): String {
        var actorText = "[white]" + (actor?.name ?: unit?.emoji() ?: "unknown")

        if (actor?.id != null) {
            actorText = "[lightgray][${actor.id}] $actorText"
        }

        val ago = "[lightgray][${formatAgo(time * 1000)}] "

        return ago + when (type) {
            HistoryType.Rotate ->
                "$actorText [tan]rotated [white]${block.emoji()}"

            HistoryType.BreakBlock ->
                "$actorText [red]broken [white]${block.emoji()}"

            HistoryType.BuildBlock ->
                "$actorText [green]built [white]${block.emoji()}"

            HistoryType.Configure -> {
                val cfg = configAsString(configAfter, block)

                Log.info("CONFIG RAW IZ $cfg (${configAfter?.javaClass})")

                "$actorText [tan]configured [white]${block.emoji()}" +
                        if (cfg.isNullOrBlank()) "" else " [lightgray]to $cfg"
            }

            HistoryType.DestroyBlock ->
                "[white]${team.emoji}${block.emoji()} [red]destroyed"

            else ->
                "$actorText [tan]${type.name} [white]${block.emoji()}"
        }
    }
}