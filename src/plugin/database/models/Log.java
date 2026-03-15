package plugin.database.models;

import plugin.PVars;

import java.sql.Timestamp;
import java.time.Instant;

import static plugin.database.Database.executeUpdate;

public class Log {
    public int id, serverId;
    public Integer playerId = null;
    public String type, message;
    public Instant timestamp;

    protected boolean fromDb = false;

    // created from db
    public Log(int id, int serverId, Integer playerId, String type, String message, Instant timestamp) {
        this.id = id;
        this.serverId = serverId;
        this.playerId = playerId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    // created from action
    public Log(Integer playerId, String type, String message) {
        this.id = -1;
        this.serverId = PVars.serverId;
        this.playerId = playerId;
        this.type = type;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public boolean write() {
        if (fromDb) return false;
        this.fromDb = true;
        return executeUpdate(
                """
                        INSERT INTO logs (type, message, timestamp, server_id, player_id) VALUES (?, ?, ?, ?, ?)
                        """,
                stmt -> {
                    stmt.setString(1, type);
                    stmt.setString(2, message);
                    stmt.setTimestamp(3, Timestamp.from(timestamp));
                    stmt.setInt(4, serverId);
                    if (playerId == null) {
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        stmt.setInt(5, playerId);
                    }
                }
        );
    }
}
