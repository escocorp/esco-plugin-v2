package plugin.replays

import arc.struct.LongMap
import arc.util.Timer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.game.Team
import mindustry.gen.Call
import plugin.history.HistoryStack
import plugin.history.HistoryType

@OptIn(ExperimentalSerializationApi::class)
fun saveReplay(history: LongMap<HistoryStack>, mapName: String): ByteArray {

    val players = ArrayList<ReplayPlayer>()
    val playerIndexMap = HashMap<Int, Int>() // playerId -> local replay id

    val map = HashMap<Long, ReplayStack>()

    history.forEach { entry ->
        val stack = ReplayStack()

        entry.value.stack.forEach { r ->
            if (!r.center) return@forEach

            val dbId = r.playerId

            val index = if (dbId == null) {
                null
            } else {
                playerIndexMap.getOrPut(dbId) {
                    val newIndex = players.size
                    players.add(ReplayPlayer(r.playerName, dbId))
                    newIndex
                }
            }

            stack.add(
                ReplayRecord(
                    playerIndex = index,
                    type = r.type.ordinal,
                    blockId = r.block.id,
                    unitId = r.unit?.id,
                    time = r.time,
                    team = r.team.id,
                    rotation = r.rotation
                )
            )
        }

        map[entry.key] = stack
    }

    return ProtoBuf.encodeToByteArray(
        Replay(mapName, players, map)
    )
}

fun playReplay(replay: HashMap<Long, ReplayStack>) {
    val events = mutableListOf<ReplayEvent>()

    replay.forEach { entry ->
        val key = entry.key
        val value = entry.value
        value.stack.forEach { r ->
            events += ReplayEvent(key.toInt(), r)
        }
    }

    events.sortBy { it.record.time }
    val start = events.first().record.time
    val end = events.last().record.time
    events.forEach { event ->
        val delay = (event.record.time - start) / 1000f
        Timer.schedule({
            if (event.record.time == end) {
                Call.sendMessage("Done!")
            }
            applyEvent(event)
        }, delay)
    }
}

private fun applyEvent(event: ReplayEvent) {
    val record = event.record
    val tile = Vars.world.tile(event.pos)
    tile ?: return
    when (event.record.type) {
        HistoryType.BuildBlock.ordinal -> {
            tile.setNet(Vars.content.block(record.blockId.toInt()), Team.get(record.team), record.rotation)
        }

        HistoryType.DestroyBlock.ordinal,
        HistoryType.BreakBlock.ordinal -> {
            tile.setNet(Blocks.air)
        }
    }
}