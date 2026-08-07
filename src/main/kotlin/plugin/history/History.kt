package plugin.history

import arc.math.geom.Point2
import arc.struct.LongMap
import mindustry.game.Team
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.Tile

object History {
    val history: LongMap<HistoryStack> = LongMap()

    private val actors = HashMap<Int, HistoryActor>()

    fun getMessage(pos: Int): String {
        val sb = StringBuilder()

        val x = Point2.x(pos).toInt()
        val y = Point2.y(pos).toInt() // int int

        sb.append("[$x] [$y]")

        synchronized(history) {
            val stack = history.get(pos.toLong()) // long

            if (stack != null) {
                for (i in 0..<stack.size()) {
                    sb.append("\n").append(stack.stack.get(i).getMessage())
                }
            }
        }

        return sb.toString()
    }

    fun copy(): LongMap<HistoryStack> {
        return synchronized(history) {
            val copyMap = LongMap<HistoryStack>(history.size)

            history.forEach { entry ->
                val originalStack = entry.value

                if (originalStack != null) {
                    val stackCopy = HistoryStack()
                    stackCopy.stack.addAll(originalStack.stack)
                    copyMap.put(entry.key, stackCopy)
                }
            }

            copyMap
        }
    }

    fun write(
        tile: Tile?,
        playerName: String?,
        playerId: Int?,
        type: HistoryType,
        block: Block,
        unit: UnitType?,
        team: Team,
        rotation: Int,
        configAfter: Any? = null,
    ) {
        if (tile == null) return

        val time = System.currentTimeMillis()

        val actor = getActor(playerName, playerId)

        val record = HistoryRecord(
            actor,
            type,
            block,
            unit,
            time,
            false,
            team,
            rotation,
            configAfter
        )

        tile.getLinkedTiles { t ->
            val pos = t.pos().toLong()

            if (t.isCenter) {
                addTile(
                    pos,
                    record.copy(center = true)
                )
            } else {
                addTile(pos, record)
            }
        }
    }

    private fun getActor(name: String?, id: Int?): HistoryActor? {
        if (name == null && id == null) return null

        if (id == null) {
            return HistoryActor(name, null)
        }

        return actors.getOrPut(id) {
            HistoryActor(name, id)
        }
    }

    private fun addTile(pos: Long, record: HistoryRecord) {
        synchronized(history) {
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
    }

    fun clear() {
        synchronized(history) {
            history.clear()
            actors.clear()
        }
    }
}