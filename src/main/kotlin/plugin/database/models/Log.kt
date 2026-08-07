package plugin.database.models

import arc.util.Strings
import mindustry.net.Administration
import mindustry.net.Administration.PlayerAction
import plugin.PVars
import plugin.database.Database.executeUpdate
import java.sql.Timestamp
import java.time.Instant

class Log {

    val id: Int
    val serverId: Int
    val playerId: Int?
    val type: String
    val message: String
    val timestamp: Instant

    private var fromDb = false

    // Created from DB
    constructor(
        id: Int,
        serverId: Int,
        playerId: Int?,
        type: String,
        message: String,
        timestamp: Instant
    ) {
        this.id = id
        this.serverId = serverId
        this.playerId = playerId
        this.type = type
        this.message = message
        this.timestamp = timestamp
        this.fromDb = true
    }

    // Created from action
    constructor(
        playerId: Int?,
        type: String,
        message: String
    ) {
        this.id = -1
        this.serverId = PVars.serverId
        this.playerId = playerId
        this.type = type
        this.message = message
        this.timestamp = Instant.now()
    }

    fun write(): Boolean {
        if (fromDb) return false
        fromDb = true

        return executeUpdate(
            """
            INSERT INTO logs (type, message, timestamp, server_id, player_id)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
        ) { stmt ->
            stmt.setString(1, type)
            stmt.setString(2, message)
            stmt.setTimestamp(3, Timestamp.from(timestamp))
            stmt.setInt(4, serverId)

            if (playerId == null) {
                stmt.setNull(5, java.sql.Types.INTEGER)
            } else {
                stmt.setInt(5, playerId)
            }
        }
    }
}


// region Log

fun putLog(type: String, message: String) {
    PVars.logsBuffer.add(Log(null, type, message))
}

fun putLog(pid: Int, type: String, message: String) {
    PVars.logsBuffer.add(Log(pid, type, message))
}

fun putLog(action: PlayerAction, pd: PlayerData) {
    val message: String?
    val type = action.type

    val tile = action.tile ?: return
    if (tile.block().isAir) return

    message = when (type) {
        Administration.ActionType.breakBlock -> Strings.format(
            "Player break block @ at @ @",
            tile.block().name,
            tile.x,
            tile.y
        )

        Administration.ActionType.placeBlock -> Strings.format(
            "Player placed block @ at @ @",
            action.block,
            tile.x,
            tile.y
        )

        Administration.ActionType.rotate -> Strings.format(
            "Player rotated block @ at @ @",
            tile.block().name,
            tile.x,
            tile.y
        )

        Administration.ActionType.configure -> Strings.format(
            "Player configured block @ at @ @",
            tile.block().name,
            tile.x,
            tile.y
        )

        else -> return
    }

    putLog(pd.id, "action", message)
}

// endregion
