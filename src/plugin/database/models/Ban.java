package plugin.database.models;

import mindustry.gen.Player;
import plugin.Bundle;
import plugin.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;
import arc.util.Time;

import static plugin.PVars.discordLink;
import static plugin.PVars.serverId;
import static plugin.utils.Utils.formatTime;
import plugin.PVars;

public class Ban {
    public int id, playerId, adminId;
    public boolean active;
    public Instant banTime, unbanTime;
    public String reason;

    Ban(int id, int playerId, int adminId, boolean active, Instant banTime, Instant unbanTime, String reason) {
        this.id = id;
        this.playerId = playerId;
        this.active = active;
        this.adminId = adminId;
        this.banTime = banTime;
        this.unbanTime = unbanTime;
        this.reason = reason;
    }
    
    public void kickPlayer(Player p) {
        String time;
        if(unbanTime == null) {
            time = "Never (perm-ban)";
        } else {
            time = formatTime((unbanTime.toEpochMilli() - Time.millis()) / 1000);
        }
        p.kick(
                MessageFormat.format(
                        Bundle.get("banned"),
                        reason,
                        time,
                        discordLink,
                        id
                ),
                0
        );
    }

    public static boolean ban(int pid, int aid, String reason, long unban) {
        return Database.executeUpdate(
                "INSERT INTO bans (player_id, admin_id, reason, unban_time) VALUES (?, ?, ?, ?)",
                stmt->{
                    stmt.setInt(1, pid);
                    stmt.setInt(2, aid);
                    stmt.setString(3, reason);
                    stmt.setTimestamp(4, getTimestamp(unban));
                }
        );
    }

    public static boolean ban(int pid, Player admin, String reason, long unban) {
        return Database.executeUpdate(
                """
        INSERT INTO bans (player_id, admin_id, reason, unban_time)
        VALUES (
            ?,
            (SELECT id FROM players WHERE uuid = ?),
            ?,
            ?
        )
    """,
                stmt -> {
                    stmt.setInt(1, pid);
                    stmt.setString(2, admin.uuid());
                    stmt.setString(3, reason);
                    stmt.setTimestamp(4, getTimestamp(unban));
                }
        );
    }

    public static boolean ban(Player player, Player admin, String reason, long unban) {
        return Database.executeUpdate(
                """
        INSERT INTO bans (player_id, admin_id, reason, unban_time)
        VALUES (
            (SELECT id FROM players WHERE uuid = ?),
            (SELECT id FROM players WHERE uuid = ?),
            ?,
            ?
        )
    """,
                stmt -> {
                    stmt.setString(1, player.uuid());
                    stmt.setString(2, admin.uuid());
                    stmt.setString(3, reason);
                    stmt.setTimestamp(4, getTimestamp(unban));
                }
        );
    }

    public static Timestamp getTimestamp(long seconds) {
        if(seconds < 0) return null; // перм бан

        long millis = seconds * 1000;
        return new Timestamp(System.currentTimeMillis() + millis);
    }

    public static Optional<Ban> getBan(int id) {
        return Database.executeQueryAsync(
                "SELECT * FROM bans WHERE id = ?",
                stmt->stmt.setInt(1, id),
                Ban::getBan
        );
    }

    public static Optional<Ban> getBan(Player player) {
        return Database.executeQueryAsync(
                """
SELECT b.*
FROM bans b
JOIN players p ON p.id = b.player_id
WHERE b.active = true
  AND (b.unban_time IS NULL OR b.unban_time > NOW())
  AND (
        p.uuid = ?
        OR p.last_ip = ?
        OR EXISTS (
            SELECT 1
            FROM usid_list ul
            WHERE ul.player_id = p.id
              AND ul.usid = ?
              AND ul.server = ?
        )
  )
LIMIT 1;
                """,
                stmt -> {
                    stmt.setString(1, player.uuid());
                    stmt.setString(2, player.ip());
                    stmt.setString(3, player.usid());
                    stmt.setInt(4, serverId);
                },
                Ban::getBan
        );
    }

    public static Ban getBan(ResultSet rs) throws SQLException {
        int banId = rs.getInt("id");

        Timestamp banTs = rs.getTimestamp("ban_time");
        Timestamp unbanTs = rs.getTimestamp("unban_time");

        Instant banTime = banTs != null ? banTs.toInstant() : Instant.now();
        Instant unbanTime = unbanTs != null ? unbanTs.toInstant() : null;

        return new Ban(
                banId,
                rs.getInt("player_id"),
                rs.getInt("admin_id"),
                rs.getBoolean("active"),
                banTime,
                unbanTime,
                rs.getString("reason")
        );
    }
}