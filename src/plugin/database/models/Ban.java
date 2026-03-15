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
import static plugin.utils.UtilsKt.formatTime;
import plugin.PVars;

public class Ban {
    public int id, playerId, adminId;
    public boolean active;
    public Instant banTime, unbanTime;
    public String reason;

    public Ban(int id, int playerId, int adminId, boolean active, Instant banTime, Instant unbanTime, String reason) {
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
}