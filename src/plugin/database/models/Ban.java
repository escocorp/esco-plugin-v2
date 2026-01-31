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

import static plugin.Utils.formatTime;

public class Ban {
    int id, playerId, adminId;
    boolean active;
    Instant banTime, unbanTime;
    String reason;

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
        p.kick(
                MessageFormat.format(
                        Bundle.get("banned"),
                        reason,
                        formatTime((unbanTime.toEpochMilli() - Time.millis()) / 1000)
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
        long millis = seconds * 1000;
        Timestamp unbanTs = new Timestamp(System.currentTimeMillis() + millis);
        return unbanTs;
    }

    public static Optional<Ban> getBan(Player player) {
        return Database.executeQueryAsync(
                """
                SELECT b.*
FROM bans b
JOIN players p ON b.player_id = p.id
WHERE p.uuid = ?
  AND b.active = true
  AND (b.unban_time IS NULL OR b.unban_time > NOW())
LIMIT 1;
                """,
                stmt->stmt.setString(1, player.uuid()),
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