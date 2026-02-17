package plugin.database.models;

import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.Time;
import mindustry.gen.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static plugin.database.Database.executeQueryAsync;
import static plugin.database.Database.executeUpdate;

public class PlayerStats {
    public int id, playerId;
    public long playtime;
    public int blocksBuild, blocksBroken;

    public PlayerStats(int id, int playerId, long playtime, int blocksBuild, int blocksBroken) {
        this.id = id;
        this.playerId = playerId;
        this.playtime = playtime; // sec!!
        this.blocksBuild = blocksBuild;
        this.blocksBroken = blocksBroken;
    }

    public static ObjectMap<String, PlayerStats> cache = new ObjectMap<>();
    public static ObjectMap<String, Long> joinTime = new ObjectMap<>();

    public boolean write() {
        return executeUpdate("UPDATE statistics SET playtime = ?, blocks_build = ?, blocks_broken = ? WHERE id = ?",
                stmt->{
            stmt.setLong(1, playtime);
            stmt.setInt(2, blocksBuild);
            stmt.setInt(3, blocksBroken);
            stmt.setInt(4, id);
                });
    }

    public static Optional<PlayerStats> getPlayerStats(Player player) {
        String uuid = player.uuid();

        PlayerStats cached = cache.get(uuid);
        if (cached != null)
            return Optional.of(cached);

        Optional<PlayerStats> opt = executeQueryAsync(
                "SELECT * from statistics WHERE player_id in (SELECT id FROM players WHERE uuid = ?)",
                stmt -> stmt.setString(1, uuid),
                PlayerStats::getPlayerStats
        );

        opt.ifPresent(stats -> cache.put(uuid, stats));

        return opt;
    }

    public PlayerStats adjBlocksBuild() {
        this.blocksBuild += 1;
        return this;
    }

    public PlayerStats adjBlocksBroken() {
        this.blocksBroken += 1;
        return this;
    }

    public static void setJoinTime(Player p) {
        joinTime.put(p.uuid(), Time.millis());
    }

    public PlayerStats update(Player player, boolean purge) {
        long time = joinTime.get(player.uuid());
        playtime += (Time.millis() - time) / 1000; // to sec

        joinTime.remove(player.uuid());
        if(!purge)
            setJoinTime(player);
        else
            write();

        return this;
    }

    public static void purge(Player player) {
        PlayerStats stats = cache.get(player.uuid());

        if(stats != null) {
            stats.update(player, true);
        }

        cache.remove(player.uuid());
    }

    public static Optional<PlayerStats> getPlayerStats(int pid) {
        return executeQueryAsync(
                "SELECT * from statistics WHERE player_id = ?",
                stmt->stmt.setInt(1, pid),
                PlayerStats::getPlayerStats
        );
    }

    public static PlayerStats getPlayerStats(ResultSet rs) throws SQLException {
        return new PlayerStats(
                rs.getInt("id"),
                rs.getInt("player_id"),
                rs.getLong("playtime"),
                rs.getInt("blocks_build"),
                rs.getInt("blocks_broken")
        );
    }
}
