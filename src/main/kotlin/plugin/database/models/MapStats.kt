/*
Auto-generated on 2026-04-25T22:39:13.017Z
Based on migrations.sql#L99 (table: maps)
*/
package plugin.database.models

import plugin.PVars.serverId
import plugin.database.Database.executeQuery
import plugin.database.Database.executeUpdate
import java.sql.ResultSet
import java.sql.SQLException

data class MapStats(
    val id: Int,
    val name: String,
    val server: Int,
    val minWave: Int,
    val avgWave: Int,
    val maxWave: Int,
    val minPlaytime: Int,
    val avgPlaytime: Int,
    val maxPlaytime: Int,
    val wins: Int,
    val loses: Int,
    val skips: Int
)


// region map stats

@Throws(SQLException::class)
fun getMapStats(rs: ResultSet): MapStats {
    return MapStats(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getInt("server"),
        rs.getInt("min_wave"),
        rs.getInt("avg_wave"), // calculated in real time
        rs.getInt("max_wave"),
        rs.getInt("min_playtime"),
        rs.getInt("avg_playtime"), // calculated in real time
        rs.getInt("max_playtime"),
        rs.getInt("wins"),
        rs.getInt("loses"),
        rs.getInt("skips")
    )
}

fun getMapStats(id: Int): MapStats? {
    return executeQuery(
        """
        SELECT
            id,
            name,
            server,
            min_wave,
            (min_wave + max_wave) / 2 AS avg_wave,
            max_wave,
            min_playtime,
            (min_playtime + max_playtime) / 2 AS avg_playtime,
            max_playtime,
            wins,
            loses,
            skips
        FROM maps
        WHERE id = ?
        """.trimIndent(),
        { stmt -> stmt.setInt(1, id) },
        { rs -> getMapStats(rs) }
    )
}

fun createOrGetMapStats(name: String): MapStats? {
    return executeQuery(
        """
        WITH inserted AS (
            INSERT INTO maps (name, server)
            VALUES (?, ?)
            ON CONFLICT (name, server) DO NOTHING
            RETURNING *
        )
        SELECT
            id,
            name,
            server,
            min_wave,
            (min_wave + max_wave) / 2 AS avg_wave,
            max_wave,
            min_playtime,
            (min_playtime + max_playtime) / 2 AS avg_playtime,
            max_playtime,
            wins,
            loses,
            skips
        FROM inserted
        UNION ALL
        SELECT
            id,
            name,
            server,
            min_wave,
            (min_wave + max_wave) / 2 AS avg_wave,
            max_wave,
            min_playtime,
            (min_playtime + max_playtime) / 2 AS avg_playtime,
            max_playtime,
            wins,
            loses,
            skips
        FROM maps
        WHERE name = ? AND server = ?
          AND NOT EXISTS (SELECT 1 FROM inserted)
        LIMIT 1
        """.trimIndent(),
        { stmt ->
            stmt.setString(1, name)
            stmt.setInt(2, serverId)
            stmt.setString(3, name)
            stmt.setInt(4, serverId)
        },
        { rs -> getMapStats(rs) }
    )
}

fun updateMapStats(
    name: String,
    minWave: Int,
    maxWave: Int,
    minPlaytime: Int,
    maxPlaytime: Int,
    wins: Int,
    loses: Int,
    skips: Int
): Boolean {
    return executeUpdate(
        """
        UPDATE maps SET
            min_wave = ?,
            max_wave = ?,
            min_playtime = ?,
            max_playtime = ?,
            wins = ?,
            loses = ?,
            skips = ?
        WHERE name = ? AND server = ?
        """.trimIndent()
    ) { stmt ->
        stmt.setInt(1, minWave)
        stmt.setInt(2, maxWave)
        stmt.setInt(3, minPlaytime)
        stmt.setInt(4, maxPlaytime)
        stmt.setInt(5, wins)
        stmt.setInt(6, loses)
        stmt.setInt(7, skips)
        stmt.setString(8, name)
        stmt.setInt(9, serverId)
    }
}

fun getNextMap(excluded: String): String? {
    return executeQuery(
        "SELECT name, loses + wins + skips AS rounds_total FROM maps " +
                "WHERE name != ? AND server == ? " +
                "ORDER BY rounds_total LIMIT 1",
        { stmt ->
            stmt.setString(1, excluded)
            stmt.setInt(2, serverId)
        },
        { rs ->
            rs.getString("name")
        }
    )
}

// endregion