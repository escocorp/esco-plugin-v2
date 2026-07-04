package plugin.database.models

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Time
import arc.util.Timekeeper
import mindustry.gen.Groups
import mindustry.gen.Player
import org.postgresql.util.PGobject
import plugin.PVars
import plugin.PVars.serverId
import plugin.database.Database
import plugin.database.Database.executeQuery
import plugin.database.Database.executeQueryList
import plugin.database.Database.logExpectedCacheMiss
import plugin.database.Database.playerDataCache
import plugin.utils.getUDPAddress
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Optional

class PlayerData(
    var id: Int, var uuid: String?, var discordId: Long?, var prefs: PlayerPrefs, var lastName: String?, // stats
    var playtime: Long, var blocksBuild: Int, var blocksBroken: Int, var balance: Int, var wavesSurvived: Int
) {
    var cachedUsid: String? = null

    @Transient
    var lastGambling: Timekeeper? = null

    fun getUsid(): String? {
        if (cachedUsid != null) {
            return cachedUsid
        }
        val usidOpt = executeQuery(
            """
                        SELECT usid FROM usid_list
                        WHERE player_id = ? AND server = ?
                        
                        """.trimIndent(),
            { stmt: PreparedStatement ->
                stmt.setInt(1, id)
                stmt.setInt(2, serverId)
            },
            { rs: ResultSet -> rs.getString("usid") }
        )
        if (usidOpt != null) cachedUsid = usidOpt
        return usidOpt
    }

    fun updateDiscordId(dsid: Long): Boolean {
        val updated = Database.executeUpdate(
            """
                        UPDATE players SET discord_id = ?
                        WHERE id = ?
                        
                        """.trimIndent()
        ) { stmt: PreparedStatement ->
            stmt.setLong(1, dsid)
            stmt.setInt(2, id)
        }
        if (updated) this.discordId = dsid
        return updated
    }

    fun updatePrefs(): Boolean {
        try {
            val `object` = PGobject()
            `object`.setType("jsonb")
            `object`.setValue(PVars.objectMapper.writeValueAsString(prefs))
            return Database.executeUpdate(
                """
                            UPDATE players SET prefs = ? WHERE id = ?
                            
                            """.trimIndent()
            ) { stmt: PreparedStatement ->
                stmt.setObject(1, `object`)
                stmt.setInt(2, id)
            }
        } catch (e: Exception) {
            Log.err(e)
            return false
        }
    }

    fun setLastGambling(time: Timekeeper): PlayerData {
        lastGambling = time
        return this
    }

    fun write(): Boolean {
        return Database.executeUpdate(
            "UPDATE players SET playtime = ?, blocks_build = ?, blocks_broken = ?, balance = ?, waves_survived = ? WHERE id = ?"
        ) { stmt: PreparedStatement? ->
            stmt!!.setLong(1, playtime)
            stmt.setInt(2, blocksBuild)
            stmt.setInt(3, blocksBroken)
            stmt.setInt(4, balance)
            stmt.setInt(5, wavesSurvived)
            stmt.setInt(6, id)
        }
    }

    fun adjBlocksBuild(): PlayerData {
        this.blocksBuild += 1
        if (blocksBuild % 50 == 0) adjBalance(PVars.gamemode.blockCost)
        return this
    }

    fun adjBlocksBroken(): PlayerData {
        this.blocksBroken += 1
        return this
    }

    fun adjWavesSurvived(): PlayerData {
        this.wavesSurvived += 1
        adjBalance(PVars.gamemode.waveCost)
        return this
    }

    fun adjWins(): PlayerData {
        return adjBalance(PVars.gamemode.winCost)
    }

    @JvmOverloads
    fun adjBalance(count: Int = 1): PlayerData {
        this.balance += count
        return this
    }

    fun subBalance(count: Int): PlayerData {
        this.balance -= count
        return this
    }

    fun update(player: Player, purge: Boolean): PlayerData {
        synchronized(joinTime) {
            val time: Long? = joinTime.remove(player.uuid())
            if (time != null) {
                val deltaSec = (Time.millis() - time) / 1000
                if (deltaSec > 0) playtime += deltaSec
            }
            if (!purge) setJoinTime(player)
        }
        if (purge) write()

        return this
    }

    companion object {
        var joinTime: ObjectMap<String?, Long?> = ObjectMap<String?, Long?>()

        fun setJoinTime(p: Player) {
            joinTime.put(p.uuid(), Time.millis())
        }
    }
}


// region PlayerData

fun deepSearchNames(player: Player): List<String> {
    return executeQueryList(
        """
                SELECT DISTINCT p.last_name
                FROM players p
                LEFT JOIN usid_list u ON u.player_id = p.id
                LEFT JOIN connections c ON c.player_id = p.id
                WHERE p.last_ip = ?
                   OR u.usid = ?
                   OR c.address = ?
                
                """.trimIndent(),
        { stmt: PreparedStatement ->
            stmt.setString(1, player.ip())
            stmt.setString(2, player.usid())
            stmt.setString(3, player.ip())
        },
        { rs: ResultSet -> rs.getString("last_name") }
    )
}

fun deepSearch(player: Player): List<PlayerData> {
    return executeQueryList(
        """
                SELECT DISTINCT p.*
                FROM players p
                LEFT JOIN usid_list u ON u.player_id = p.id
                LEFT JOIN connections c ON c.player_id = p.id
                WHERE p.last_ip = ?
                   OR u.usid = ?
                   OR c.address = ?
                
                """.trimIndent(),
        { stmt: PreparedStatement ->
            stmt.setString(1, player.ip())
            stmt.setString(2, player.usid())
            stmt.setString(3, player.ip())
        },
        { rs: ResultSet -> getPlayerData(rs) }
    )
}

fun getPlayerById(id: Int): Optional<Player> {
    val pd = getPlayerData(id)
    var p: Player? = null
    if (pd != null) {
        p = Groups.player.find { player: Player -> player.uuid() == pd.uuid }
    }
    return Optional.ofNullable<Player>(p)
}

fun getOrCreatePlayerData(p: Player): PlayerData? {
    if (playerDataCache.containsKey(p)) {
        return playerDataCache.get(p)
    }

    val pd = executeQuery(
        """
                        WITH update_players AS (
                            INSERT INTO players (uuid, last_name, last_ip, locale, color)
                            VALUES (?, ?, ?::INET, ?, ?)
                            ON CONFLICT (uuid) DO UPDATE SET
                                last_name = EXCLUDED.last_name,
                                last_ip   = EXCLUDED.last_ip,
                                color     = EXCLUDED.color,
                                locale    = EXCLUDED.locale,
                                last_seen = NOW()
                            RETURNING id, uuid, last_name, last_ip, locale, color, discord_id, prefs, playtime, blocks_build, blocks_broken, waves_survived, balance
                        ),
                        insert_usid AS (
                            INSERT INTO usid_list (player_id, usid, server)
                            SELECT id, ?, ?
                            FROM update_players
                            ON CONFLICT DO NOTHING
                        ),
                        insert_connection AS (
                            INSERT INTO connections(player_name, address, address_udp, server_id, player_id)
                            SELECT last_name, last_ip, ?::INET, ?, id
                            FROM update_players
                        ),
                        insert_stats AS (
                            INSERT INTO statistics (player_id)
                            SELECT id
                            FROM update_players
                            ON CONFLICT (player_id) DO NOTHING
                        )
                        SELECT *
                        FROM update_players;
                        
                        """.trimIndent(),
        { stmt: PreparedStatement ->
            stmt.setString(1, p.uuid())
            stmt.setString(2, p.name())
            stmt.setString(3, p.ip())
            stmt.setString(4, p.locale)
            stmt.setString(5, p.color.toString())
            stmt.setString(6, p.usid())
            stmt.setInt(7, serverId)
            stmt.setString(8, getUDPAddress(p))
            stmt.setInt(9, serverId)
        },
        { rs: ResultSet -> getPlayerData(rs) }
    )
    if (!playerDataCache.containsKey(p) && pd != null) playerDataCache.put(p, pd)
    return pd
}

fun getPlayerData(id: Int): PlayerData? {
    return executeQuery(
        "SELECT * FROM players WHERE id = ?",
        { stmt: PreparedStatement -> stmt.setInt(1, id) },
        { rs: ResultSet -> getPlayerData(rs) }
    )
}

fun getPlayerData(player: Player): PlayerData? {
    if (playerDataCache.containsKey(player)) return playerDataCache.get(player)
    logExpectedCacheMiss(player, "playerDataCache")

    return executeQuery(
        "SELECT * FROM players WHERE uuid = ?",
        { stmt: PreparedStatement -> stmt.setString(1, player.uuid()) },
        { rs: ResultSet -> getPlayerData(rs) }
    )
}

fun Player.getData(): PlayerData? {
    return getPlayerData(this)
}

/**
 * no cache
 * */
fun getPlayerData(uuid: String): PlayerData? {
    return executeQuery(
        "SELECT * FROM players WHERE uuid = ?",
        { stmt: PreparedStatement -> stmt.setString(1, uuid) },
        { rs: ResultSet -> getPlayerData(rs) }
    )
}

fun getPlayerId(player: Player): Int? {
    if (playerDataCache.containsKey(player)) return playerDataCache.get(player).id
    logExpectedCacheMiss(player, "playerDataCache(id)")

    return executeQuery(
        "SELECT id FROM players WHERE uuid = ?",
        { stmt: PreparedStatement -> stmt.setString(1, player.uuid()) },
        { rs: ResultSet -> rs.getInt("id") }
    )
}

/*
* No caching!!!
* */
fun getPlayerId(uuid: String): Int? {
    return executeQuery(
        "SELECT id FROM players WHERE uuid = ?",
        { stmt: PreparedStatement -> stmt.setString(1, uuid) },
        { rs: ResultSet -> rs.getInt("id") }
    )
}

@Throws(SQLException::class)
fun getPlayerData(rs: ResultSet): PlayerData {
    val prefs = try {
        PVars.objectMapper.readValue(rs.getString("prefs"), PlayerPrefs::class.java)
    } catch (e: Exception) {
        Log.err(e)
        PlayerPrefs()
    }

    val discordIdRaw = rs.getLong("discord_id")
    val discordId: Long? = if (rs.wasNull()) null else discordIdRaw

    return PlayerData(
        rs.getInt("id"),
        rs.getString("uuid"),
        discordId,
        prefs,
        rs.getString("last_name"),
        rs.getLong("playtime"),
        rs.getInt("blocks_build"),
        rs.getInt("blocks_broken"),
        rs.getInt("balance"),
        rs.getInt("waves_survived"),
    )
}

private fun ensureJoinTimeTracked(player: Player) {
    if (!PlayerData.joinTime.containsKey(player.uuid())) {
        PlayerData.setJoinTime(player)
    }
}

// endregion