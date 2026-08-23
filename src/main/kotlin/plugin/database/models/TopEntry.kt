package plugin.database.models

import plugin.database.Database.executeQueryList
import java.sql.PreparedStatement
import java.sql.ResultSet

class TopEntry(
    val id: Int,
    val name: String?,
    val value: Long,
)

private fun getTopBy(
    column: String,
    limit: Int,
): List<TopEntry> =
    executeQueryList(
        "SELECT id, last_name, $column AS value FROM players ORDER BY $column DESC LIMIT ?",
        { stmt: PreparedStatement -> stmt.setInt(1, limit) },
        { rs: ResultSet -> TopEntry(rs.getInt("id"), rs.getString("last_name"), rs.getLong("value")) },
    )

fun getTopByBalance(limit: Int): List<TopEntry> = getTopBy("balance", limit)

fun getTopByBlocksBuild(limit: Int): List<TopEntry> = getTopBy("blocks_build", limit)

fun getTopByPlaytime(limit: Int): List<TopEntry> = getTopBy("playtime", limit)
