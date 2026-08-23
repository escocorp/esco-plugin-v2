package plugin.database.models

import plugin.database.Database.executeQueryList
import java.sql.PreparedStatement
import java.sql.ResultSet

class BalanceEntry(
    val id: Int,
    val name: String?,
    val balance: Int,
)

fun getTopByBalance(limit: Int): List<BalanceEntry> =
    executeQueryList(
        "SELECT id, last_name, balance FROM players ORDER BY balance DESC LIMIT ?",
        { stmt: PreparedStatement -> stmt.setInt(1, limit) },
        { rs: ResultSet -> BalanceEntry(rs.getInt("id"), rs.getString("last_name"), rs.getInt("balance")) },
    )
