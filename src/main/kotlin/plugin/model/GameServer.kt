package plugin.model

import arc.struct.Seq
import arc.util.Log
import arc.util.Strings

data class GameServer(
    val name: String,
    val ip: String,
    val port: Int,
)

val gameServers: Seq<GameServer> = Seq()

/**
 * Fills [gameServers] from a `name:ip:port` comma separated string.
 * Malformed entries are skipped with a warning, so a typo in the config
 * cannot take the whole list down.
 * */
fun loadGameServers(raw: String?) {
    gameServers.clear()

    if (raw.isNullOrBlank()) return

    for (entry in raw.split(",")) {
        val parts = entry.trim().split(":")
        if (parts.size != 3 || !Strings.canParseInt(parts[2].trim())) {
            Log.warn("Malformed server entry '@', expected 'name:ip:port'", entry)
            continue
        }
        gameServers.add(GameServer(parts[0].trim(), parts[1].trim(), Strings.parseInt(parts[2].trim())))
    }

    Log.info("Loaded @ hub servers", gameServers.size)
}
