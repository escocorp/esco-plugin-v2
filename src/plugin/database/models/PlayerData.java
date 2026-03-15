package plugin.database.models;

import arc.struct.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import mindustry.gen.Player;
import org.postgresql.util.PGobject;
import plugin.PVars;
import plugin.database.Database;
import mindustry.gen.Groups;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static arc.util.Log.err;
import static plugin.PVars.*;
import static plugin.database.Database.*;
import static plugin.utils.UtilsKt.getUDPAddress;

public class PlayerData {
    public int id;
    public String uuid;
    public Long discordId;
    public PlayerPrefs prefs;

    public transient String originalName = "frog"; // set when player join

    public PlayerData setOriginalName(String s) {
        this.originalName = s;
        return this;
    }

    public PlayerData(int id, String uuid, Long discordId, PlayerPrefs prefs) {
        this.id = id;
        this.uuid = uuid;
        this.discordId = discordId;
        this.prefs = prefs;
    }

    public boolean updateDiscordId(Long dsid) {
        boolean updated = executeUpdate(
        """
                UPDATE players SET discord_id = ?
                WHERE id = ?
                """,
                stmt->{
                    stmt.setLong(1, dsid);
                    stmt.setInt(2, id);
                }
        );
        if(updated)
            this.discordId = dsid;
        return updated;
    }

    public boolean updatePrefs() {
        try {
            PGobject object = new PGobject();
            object.setType("jsonb");
            object.setValue(objectMapper.writeValueAsString(prefs));
            return executeUpdate(
                    """
                            UPDATE players SET prefs = ? WHERE id = ?
                            """,
                    stmt -> {
                        stmt.setObject(1, object);
                        stmt.setInt(2, id);
                    }
            );
        } catch (Exception e) {
            err(e);
            return false;
        }
    }

    public Optional<String> getUsid() {
        return executeQueryAsync(
                """
                SELECT usid FROM usid_list
                WHERE player_id = ? AND server = ?
                """,
                stmt->{
                    stmt.setInt(1, id);
                    stmt.setInt(2, serverId);
                },
                rs->rs.getString("usid")
        );
    }
}
