package plugin.database.models

/*
Auto-generated on 2026-07-02T10:02:10.961Z
Based on migrations.sql#L119 (table: messages)
*/
import mindustry.gen.Player
import plugin.PVars
import plugin.database.Database.executeUpdate
import kotlin.time.Instant
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

data class Message(
    val id: Int /*By SERIAL*/,
    val playerId: Int,
    val unformatted: String,
    val formatted: String,
    val timestamp: Instant
)

@Throws(SQLException::class)
fun getMessage(rs: ResultSet): Message {
    return Message(
        rs.getInt("id"),
        rs.getInt("player_id"),
        rs.getString("unformatted"),
        rs.getString("formatted"),
        rs.getTimestamp("timestamp").toInstant().toKotlinInstant()
    )
}

fun putMessage(playerId: Int, unformatted: String, formatted: String, timestamp: Instant): Boolean {
    return executeUpdate(
        """
                INSERT INTO messages (player_id, unformatted, formatted, timestamp, server_id)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
        { stmt ->
            stmt.setInt(1, playerId)
            stmt.setString(2, unformatted)
            stmt.setString(3, formatted)
            stmt.setTimestamp(4, Timestamp.from(timestamp.toJavaInstant()))
            stmt.setInt(5, PVars.serverId)
        }
    )
}