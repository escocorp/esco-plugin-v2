package plugin.commands

import arc.Core
import kotlinx.coroutines.launch
import mindustry.gen.Player
import plugin.Bundle
import plugin.KVars.globalScope
import plugin.database.models.getTopByBalance
import plugin.database.models.getTopByBlocksBuild
import plugin.database.models.getTopByPlaytime
import plugin.utils.formatTime

enum class TopType {
    Balance,
    Blocks,
    Playtime,
}

/**
 * Sends the top [limit] players for [type] to the player.
 * The query runs off the main thread, the message is sent back on it.
 * */
fun showTop(
    player: Player,
    type: TopType,
    limit: Int = 10,
) {
    globalScope.launch {
        val entries =
            when (type) {
                TopType.Balance -> getTopByBalance(limit)
                TopType.Blocks -> getTopByBlocksBuild(limit)
                TopType.Playtime -> getTopByPlaytime(limit)
            }

        Core.app.post {
            val header =
                when (type) {
                    TopType.Balance -> "command.top.header"
                    TopType.Blocks -> "command.top.header.blocks"
                    TopType.Playtime -> "command.top.header.playtime"
                }

            val sb = StringBuilder(Bundle.get(header, player.locale)).append("\n")
            entries.forEachIndexed { i, entry ->
                val value =
                    when (type) {
                        TopType.Balance -> "[green]$" + entry.value
                        TopType.Blocks -> entry.value.toString()
                        TopType.Playtime -> formatTime(entry.value)
                    }
                sb
                    .append(Bundle.get("command.top.entry.generic", player.locale, i + 1, entry.name ?: "", entry.id, value))
                    .append("\n")
            }
            player.sendMessage(sb.toString().trimEnd())
        }
    }
}
