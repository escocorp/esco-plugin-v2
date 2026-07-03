package plugin.history

import mindustry.game.Team
import mindustry.type.UnitType
import mindustry.world.Block

data class HistoryRecord(
    val playerName: String?, val playerId: Int?, val type: HistoryType, val block: Block,
    val unit: UnitType?, val time: Long, val center: Boolean, val team: Team, val rotation: Int
) {
        fun getMessage(): String {
            var actor =
                "[white]" + (playerName ?: if (unit == null) "unknown" else unit.emoji())
            if (playerId != null) actor = "[lightgray][[[white]$playerId[]] $actor"
            return when (type) {
                HistoryType.Rotate -> actor + " [tan]rotated[white] " + block.emoji()
                HistoryType.BreakBlock -> actor + " [red]broken[white] " + block.emoji()
                HistoryType.BuildBlock -> actor + " [green]built[white] " + block.emoji()
                HistoryType.Configure -> actor + " [tan]configured[white] " + block.emoji()
                HistoryType.DestroyBlock -> "[white]" + team.emoji + block.emoji() + " [red]destroyed"
                else -> actor + " [tan]" + type.name + "[white] " + block.emoji()
            }
        }
}
