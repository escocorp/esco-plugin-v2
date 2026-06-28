package plugin.database.models

import arc.func.Cons
import arc.struct.ObjectMap
import arc.struct.Seq
import mindustry.gen.Player
import plugin.database.Database.executeQuery
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

enum class Permission {
    None,
    Test,
    Admin,
    Punish,
    EditMaps,
    VotekickImmune,
    EditServer,
    Vanish,
    Artv,
    Replays,
    MegaBan;

    companion object {
        val cache = ObjectMap<Player, Seq<Permission>>()

        // public static final Permission[] all = values();
        fun parsePerms(perms: Array<String>): Seq<Permission> {
            val rperms = Seq<Permission>()
            for (pname in perms) rperms.add(parsePerm(pname))
            return rperms
        }

        fun parsePerm(name: String): Permission {
            for (p in entries) if (p.name.contains(name, ignoreCase = true)) return p
            return None
        }

        fun getPerms(p: Player): Seq<Permission> {
            if (cache.containsKey(p)) return cache.get(p)

            val r = executeQuery(
                "SELECT ar.permissions\n" +
                        "        FROM players p\n" +
                        "        LEFT JOIN admins a ON a.player_id = p.id\n" +
                        "        LEFT JOIN admin_ranks ar ON ar.id = a.rank_id\n" +
                        "        WHERE p.uuid = ?",
                { stmt: PreparedStatement -> stmt.setString(1, p.uuid()) },
                { rs: ResultSet -> getPerms(rs) }
            )

            val seq = r ?: Seq.with(None)

            if (!seq.contains(None)) seq.add(None)
            cache.put(p, seq)

            return seq
        }

        fun getPermsByDiscordId(discordId: Long): Seq<Permission> {
            val r = executeQuery(
                "SELECT ar.permissions\n" +
                        "        FROM players p\n" +
                        "        LEFT JOIN admins a ON a.player_id = p.id\n" +
                        "        LEFT JOIN admin_ranks ar ON ar.id = a.rank_id\n" +
                        "        WHERE p.discord_id = ?",
                { stmt: PreparedStatement -> stmt.setLong(1, discordId) },
                { rs: ResultSet -> getPerms(rs) }
            )

            val seq = r ?: Seq.with(None)
            if (!seq.contains(None)) seq.add(None)

            return seq
        }

        @Throws(SQLException::class)
        fun getPerms(rs: ResultSet): Seq<Permission> {
            val sqlArray = rs.getArray("permissions")
            val perms: Array<String>

            if (sqlArray != null) {
                perms = sqlArray.array as Array<String>
            } else {
                perms = arrayOf("none")
            }

            return parsePerms(perms)
        }

        fun seqToString(s: Seq<Permission>): String {
            val sb = StringBuilder()
            s.each(Cons { p: Permission -> sb.append(p.name + " ") })
            return sb.toString()
        }
    }
}