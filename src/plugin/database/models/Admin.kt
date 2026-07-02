package plugin.database.models

import arc.struct.Seq
import mindustry.gen.Player
import plugin.database.Database
import plugin.database.Database.adminsCache
import plugin.database.Database.executeQuery
import plugin.database.Database.executeUpdate
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.collections.get

class Admin(
    val id: Int,
    val playerId: Int,
    val rankId: Int,
    val rankName: String,
    val perms: Seq<Permission>,
    var hidden: Boolean
) {

    fun updateHidden(hidden: Boolean): Boolean {
        val updated = executeUpdate(
            """
            UPDATE ADMINS SET
            hidden = ?
            WHERE id = ?
            """.trimIndent()
        ) { stmt ->
            stmt.setBoolean(1, hidden)
            stmt.setInt(2, id)
        }

        if (updated) {
            this.hidden = hidden
        }

        return updated
    }
}


// region Admin

fun updateAdminHidden(pid: Int, hidden: Boolean): Boolean {
    return executeUpdate(
        """
                        UPDATE ADMINS SET
                        hidden = ?
                        WHERE player_id = ?
                        
                        """.trimIndent()
    ) { stmt: PreparedStatement ->
        stmt.setBoolean(1, hidden)
        stmt.setInt(2, pid)
    }
}

fun getAdmin(player: Player): Admin? {
    if (adminsCache.containsKey(player)) return adminsCache.get(player)
    Database.logExpectedCacheMiss(player, "adminsCache")

    val a = executeQuery(
        """
                        SELECT 
                                COALESCE(ar.name, 'player') AS rank_name,
                                COALESCE(ar.permissions, ARRAY['none']) AS permissions, a.id, a.player_id, a.hidden, a.rank_id
                            FROM players p
                            LEFT JOIN admins a ON a.player_id = p.id
                            LEFT JOIN admin_ranks ar ON ar.id = a.rank_id
                            WHERE p.uuid = ?
                    
                    """.trimIndent(),
        { stmt: PreparedStatement -> stmt.setString(1, player.uuid()) },
        { rs: ResultSet -> getAdmin(rs) }
    )
    a?.let { admin: Admin ->
        adminsCache.put(player, admin)
    }
    return a
}

@Throws(SQLException::class)
fun getAdmin(rs: ResultSet): Admin {
    return Admin(
        rs.getInt("id"),
        rs.getInt("player_id"),
        rs.getInt("rank_id"),
        rs.getString("rank_name"),
        Permission.getPerms(rs),
        rs.getBoolean("hidden")
    )
}

// endregion