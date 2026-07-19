package plugin.database.models

import mindustry.gen.Player
import plugin.Bundle
import plugin.PVars.discordLink
import plugin.PVars.serverId
import plugin.database.Database.executeQuery
import plugin.database.Database.executeUpdate
import plugin.utils.formatTime
import plugin.utils.getUnbanTime
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.text.MessageFormat
import java.time.Instant
import kotlin.time.Clock
import kotlin.time.toJavaInstant

/*
Auto-generated on 2026-07-19T10:53:21.215Z
Based on migrations.sql#L55 (table: bans)
*/

class Ban(
    val id: Int /*By SERIAL*/,
    val active: Boolean,
    val canBeRemoved: Boolean,
    val playerId: Int,
    val reason: String /*By VARCHAR*/,
    val adminId: Int,
    val banTime: Instant?,
    val unbanTime: Instant?,
    val source: String
) {

    fun kickPlayer(player: Player) {
        val time = if (unbanTime == null) {
            "Never (perm-ban)"
        } else {
            formatTime(unbanTime.epochSecond - Clock.System.now().epochSeconds)
        }

        player.kick(
            MessageFormat.format(
                Bundle.get("banned", player.locale),
                reason,
                time,
                discordLink,
                id
            ),
            0
        )
    }
}


// region Ban

fun ban(pid: Int, aid: Int, reason: String?, unban: Long, source: String): Boolean {
    return executeUpdate(
        "INSERT INTO bans (player_id, admin_id, reason, unban_time, source) VALUES (?, ?, ?, ?, ?)"
    ) { stmt: PreparedStatement ->
        stmt.setInt(1, pid)
        stmt.setInt(2, aid)
        stmt.setString(3, reason)
        stmt.setTimestamp(
            4,
            getUnbanTime(unban)?.let { Timestamp.from(it.toJavaInstant()) }
        )
        stmt.setString(5, source)
    }
}

fun ban(pid: Int, admin: Player, reason: String?, unban: Long, source: String): Boolean {
    return executeUpdate(
        """
        INSERT INTO bans (player_id, admin_id, reason, unban_time, source)
        VALUES (
            ?,
            (SELECT id FROM players WHERE uuid = ?),
            ?,
            ?,
            ?
        )
    
    """.trimIndent()
    ) { stmt: PreparedStatement ->
        stmt.setInt(1, pid)
        stmt.setString(2, admin.uuid())
        stmt.setString(3, reason)
        stmt.setTimestamp(
            4,
            getUnbanTime(unban)?.let { Timestamp.from(it.toJavaInstant()) }
        )
        stmt.setString(5, source)
    }
}

fun ban(player: Player, admin: Player, reason: String?, unban: Long, source: String): Boolean {
    return executeUpdate(
        """
        INSERT INTO bans (player_id, admin_id, reason, unban_time, source)
        VALUES (
            (SELECT id FROM players WHERE uuid = ?),
            (SELECT id FROM players WHERE uuid = ?),
            ?,
            ?,
            ?
        )
    
    """.trimIndent()
    ) { stmt: PreparedStatement ->
        stmt.setString(1, player.uuid())
        stmt.setString(2, admin.uuid())
        stmt.setString(3, reason)
        stmt.setTimestamp(
            4,
            getUnbanTime(unban)?.let { Timestamp.from(it.toJavaInstant()) }
        )
        stmt.setString(5, source)
    }
}

fun getBan(id: Int): Ban? {
    return executeQuery(
        "SELECT * FROM bans WHERE id = ?",
        { stmt: PreparedStatement -> stmt.setInt(1, id) },
        { rs: ResultSet -> getBan(rs) }
    )
}

fun getBan(player: Player): Ban? {
    return executeQuery(
        """
SELECT b.*
FROM bans b
JOIN players p ON p.id = b.player_id
WHERE b.active = true
  AND (b.unban_time IS NULL OR b.unban_time > NOW())
  AND (
        p.uuid = ?
        OR p.last_ip = ?::INET
        OR EXISTS (
            SELECT 1
            FROM usid_list ul
            WHERE ul.player_id = p.id
              AND ul.usid = ?
              AND ul.server = ?
        )
  )
LIMIT 1;
                
                """.trimIndent(),
        { stmt: PreparedStatement ->
            stmt.setString(1, player.uuid())
            stmt.setString(2, player.ip())
            stmt.setString(3, player.usid())
            stmt.setInt(4, serverId)
        },
        { rs: ResultSet -> getBan(rs) }
    )
}

@Throws(SQLException::class)
fun getBan(rs: ResultSet): Ban {
    return Ban(
        rs.getInt("id"),
        rs.getBoolean("active"),
        rs.getBoolean("can_be_removed"),
        rs.getInt("player_id"),
        rs.getString("reason"),
        rs.getInt("admin_id"),
        rs.getTimestamp("ban_time")?.toInstant(),
        rs.getTimestamp("unban_time")?.toInstant(),
        rs.getString("source")
    )
}

// endregion