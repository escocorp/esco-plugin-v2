package plugin.history

import arc.func.Cons
import arc.math.geom.Point2
import arc.struct.LongMap
import mindustry.game.Team
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.Tile
import java.util.*

object History {
    val history: LongMap<HistoryStack> = LongMap<HistoryStack>()

    fun getMessage(pos: Int): String {
        val sb = StringBuilder()
        // Point2 pos2 = Point2.unpack(pos);
        val x = Point2.x(pos).toInt()
        val y = Point2.y(pos).toInt() // int int

        sb.append("[$x] [$y]")


        val stack = history.get(pos.toLong()) // long
        //stack.stack.each(s->{sb.append("\n").append(s.getMessage());});
        if (stack != null) for (i in 0..<stack.size()) {
            sb.append("\n").append(stack.stack.get(i).getMessage())
        }

        return sb.toString()
    }

    fun write(
        tile: Tile?,
        playerName: String?,
        playerId: Int?,
        type: HistoryType,
        block: Block,
        unit: UnitType?,
        team: Team,
        rotation: Int
    ) {
        if (tile == null) return

        val time = System.currentTimeMillis()

        val record = HistoryRecord(playerName, playerId, type, block, unit, time, false, team, rotation)

        tile.getLinkedTiles { t: Tile ->
            val pos = t.pos().toLong()
            if (t.isCenter) {
                addTile(pos, HistoryRecord(playerName, playerId, type, block, unit, time, true, team, rotation))
            } else {
                addTile(pos, record)
            }
        }
    }

    private fun addTile(pos: Long, record: HistoryRecord) {
        var stack = history.get(pos)

        if (stack == null) {
            stack = HistoryStack()
            history.put(pos, stack)
        }

        if (stack.size() >= 6) {
            stack.removeFirst()
        }

        stack.add(record)
    }

    fun clear() {
        history.clear()
    }
}
