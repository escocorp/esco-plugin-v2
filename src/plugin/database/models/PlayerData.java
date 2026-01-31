package plugin.database.models;

import arc.struct.ObjectMap;
import mindustry.gen.Player;
import plugin.PVars;
import plugin.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PlayerData {
    public static ObjectMap<Player, PlayerData> cache = new ObjectMap<>();
    public int id;
    public String uuid;

    PlayerData(int id, String uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public static Optional<PlayerData> getOrCreatePlayerData(Player p) {
        if(cache.containsKey(p)) {
            return Optional.of(cache.get(p));
        }

        Optional<PlayerData> pd = Database.executeQueryAsync(
                "WITH update_players AS (\n" +
                        "    INSERT INTO players (uuid, last_name, last_ip, locale, color)\n" +
                        "    VALUES (?, ?, ?, ?, ?)\n" +
                        "    ON CONFLICT (uuid) DO UPDATE SET\n" +
                        "        last_name = EXCLUDED.last_name,\n" +
                        "        last_ip   = EXCLUDED.last_ip,\n" +
                        "        color     = EXCLUDED.color,\n" +
                        "        locale    = EXCLUDED.locale\n" +
                        "    RETURNING id, uuid, last_name, last_ip, locale, color\n" +
                        "),\n" +
                        "insert_usid AS (\n" +
                        "    INSERT INTO usid_list (player_id, usid, server)\n" +
                        "    SELECT id, ?, ?\n" +
                        "    FROM update_players\n" +
                        "    ON CONFLICT (usid, server) DO NOTHING\n" +
                        ")\n" +
                        "SELECT *\n" +
                        "FROM update_players;\n",
                stmt->{
                    stmt.setString(1, p.uuid());
                    stmt.setString(2, p.name());
                    stmt.setString(3, p.ip());
                    stmt.setString(4, p.locale);
                    stmt.setString(5, p.color.toString());
                    stmt.setString(6, p.usid());
                    stmt.setString(7, PVars.gamemode.simpleName);
                },
                PlayerData::getPlayerData
        );
        if(!cache.containsKey(p) && pd.isPresent())
            cache.put(p, pd.get());
        return pd;
    }

    public static Optional<PlayerData> getPlayerData(int id) {
        return Database.executeQueryAsync(
                "SELECT * FROM players WHERE id = ?",
                stmt->stmt.setInt(1, id),
                PlayerData::getPlayerData
        );
    }

    public static Optional<PlayerData> getPlayerData(Player player) {
        if(cache.containsKey(player))
            return Optional.of(cache.get(player));

        return Database.executeQueryAsync(
                "SELECT * FROM players WHERE uuid = ?",
                stmt->stmt.setString(1, player.uuid()),
                PlayerData::getPlayerData
        );
    }

    public static PlayerData getPlayerData(ResultSet rs) throws SQLException {
        return new PlayerData(rs.getInt("id"), rs.getString("uuid"));
    }
}
