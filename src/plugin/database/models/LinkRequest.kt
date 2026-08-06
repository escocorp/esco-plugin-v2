package plugin.database.models
/*
Auto-generated on 2026-08-06T19:50:20.843Z
Based on migrations.sql#L131 (table: link_requests)
*/
import plugin.database.Database
import java.time.Instant
import java.sql.ResultSet
import java.sql.SQLException

data class LinkRequest(
    val id: Int /*By SERIAL*/,
    val state: String,
    val playerId: Int,
    val createdAt: Instant,
    val usedAt: Instant?
)

@Throws(SQLException::class)
fun getLinkRequest(rs: ResultSet): LinkRequest {
    return LinkRequest(
        rs.getInt("id"),
        rs.getString("state"),
        rs.getInt("player_id"),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("used_at")?.toInstant()
    )
}

fun newLinkRequest(state: String, playerId: Int): Boolean {
    return Database.executeUpdate(
        "INSERT INTO link_requests (state, player_id) VALUES (?, ?)",
        { stmt ->
            stmt.setString(1, state)
            stmt.setInt(2, playerId)
        }
    )
}