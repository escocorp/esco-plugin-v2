package plugin.database

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Time
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mindustry.gen.Player
import plugin.PVars.*
import plugin.database.models.Admin
import plugin.database.models.PlayerData
import plugin.database.models.putLog
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

object Database {
    val dataSource: HikariDataSource? = createDataSource()

    @JvmField
    var adminsCache = ObjectMap<Player, Admin>()

    @JvmField
    var playerDataCache = ObjectMap<Player, PlayerData>()

    private val cacheMissLogCooldown = ObjectMap<String, Long>()
    private const val CACHE_MISS_LOG_COOLDOWN_MS = 60_000L

    fun logExpectedCacheMiss(player: Player, cacheName: String) {
        // During gameplay these caches should usually be warmed on connect.
        val key = "$cacheName:${player.uuid()}"
        val now = Time.millis()
        val last = cacheMissLogCooldown.get(key, 0L)
        if (now - last < CACHE_MISS_LOG_COOLDOWN_MS) return

        cacheMissLogCooldown.put(key, now)
        putLog(
            "cache_miss",
            "Expected warm cache miss in $cacheName for uuid=${player.uuid()} name=${player.plainName()} id=${player.id} connected=${player.con?.isConnected ?: false}"
        )
    }

    private fun createDataSource(): HikariDataSource? {
        return try {
            Class.forName("org.postgresql.Driver")

            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$db"
                username = dbUser

                if (dbPassword != "empty" && dbPassword.isNotEmpty()) {
                    password = dbPassword
                }

                maximumPoolSize = 10
                minimumIdle = 3
                idleTimeout = 30000
                connectionTimeout = 5000
            }

            HikariDataSource(config)
        } catch (err: ClassNotFoundException) {
            Log.err(err)
            null
        }
    }

    @JvmStatic
    fun <T> executeQuery(
        sql: String,
        setter: StatementSetter<PreparedStatement>,
        mapper: (ResultSet) -> T
    ): T? {
        return try {
            dataSource!!.connection.use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    setter.accept(stmt)

                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            mapper(rs)
                        } else {
                            null
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            Log.err("SQL query failed @ @", sql, e)
            null
        }
    }

    @JvmStatic
    fun executeUpdate(
        sql: String,
        statementSetter: StatementSetter<PreparedStatement>
    ): Boolean {
        return try {
            dataSource!!.connection.use { conn ->
                conn.prepareStatement(sql).use { pstmt ->
                    statementSetter.accept(pstmt)
                    val updated = pstmt.executeUpdate()
                    updated > 0
                }
            }
        } catch (e: SQLException) {
            Log.err("SQL query failed @ @", sql, e)
            false
        }
    }

    @JvmStatic
    fun <T> executeQueryList(
        sql: String,
        statementSetter: StatementSetter<PreparedStatement>,
        serializer: Serializer<ResultSet, T>
    ): List<T> {
        val results = mutableListOf<T>()

        try {
            dataSource!!.connection.use { conn ->
                conn.prepareStatement(sql).use { pstmt ->
                    statementSetter.accept(pstmt)

                    pstmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            results.add(serializer.apply(rs))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            Log.err("SQL query failed @ @", sql, e)
        }

        return results
    }

    fun interface StatementSetter<T> {
        @Throws(SQLException::class)
        fun accept(t: T)
    }

    fun interface Serializer<T, R> {
        @Throws(SQLException::class)
        fun apply(t: T): R
    }
}