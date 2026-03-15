package plugin.database.models;

import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Timekeeper;
import mindustry.gen.Player;

import static plugin.PVars.gamemode;
import static plugin.database.Database.executeUpdate;
import static plugin.database.GettersKt.playerStatsCache;

public class PlayerStats {
    public int id, playerId;
    public long playtime;
    public int blocksBuild, blocksBroken, balance, wavesSurvived;

    public transient Timekeeper lastGambling;

    public PlayerStats(int id, int playerId, long playtime, int blocksBuild, int blocksBroken, int balance, int wavesSurvived) {
        this.id = id;
        this.playerId = playerId;
        this.playtime = playtime; // sec!!
        this.blocksBuild = blocksBuild;
        this.blocksBroken = blocksBroken;
        this.balance = balance;
        this.wavesSurvived = wavesSurvived;
    }

    public static ObjectMap<String, Long> joinTime = new ObjectMap<>();

    public PlayerStats setLastGambling(Timekeeper time) {
        this.lastGambling = time;
        return this;
    }

    public boolean write() {
        return executeUpdate("UPDATE statistics SET playtime = ?, blocks_build = ?, blocks_broken = ?, balance = ?, waves_survived = ? WHERE id = ?",
                stmt -> {
                    stmt.setLong(1, playtime);
                    stmt.setInt(2, blocksBuild);
                    stmt.setInt(3, blocksBroken);
                    stmt.setInt(4, balance);
                    stmt.setInt(5, wavesSurvived);
                    stmt.setInt(6, id);
                });
    }

    public PlayerStats adjBlocksBuild() {
        this.blocksBuild += 1;
        if (blocksBuild % 50 == 0)
            adjBalance(gamemode.blockCost);
        return this;
    }

    public PlayerStats adjBlocksBroken() {
        this.blocksBroken += 1;
        return this;
    }

    public PlayerStats adjWavesSurvived() {
        this.wavesSurvived += 1;
        adjBalance(gamemode.waveCost);
        return this;
    }

    public PlayerStats adjWins() {
        return adjBalance(gamemode.winCost);
    }

    public PlayerStats adjBalance() {
        return adjBalance(1);
    }

    public PlayerStats adjBalance(int count) {
        this.balance += count;
        return this;
    }

    public PlayerStats subBalance(int count) {
        this.balance -= count;
        return this;
    }

    public static void setJoinTime(Player p) {
        joinTime.put(p.uuid(), Time.millis());
    }

    public PlayerStats update(Player player, boolean purge) {
        long time = joinTime.get(player.uuid());
        playtime += (Time.millis() - time) / 1000; // to sec

        joinTime.remove(player.uuid());
        if (!purge)
            setJoinTime(player);
        else
            write();

        return this;
    }

    public static void purge(Player player) {
        PlayerStats stats = playerStatsCache.get(player.uuid());

        if (stats != null) {
            stats.update(player, true);
        }

        playerStatsCache.remove(player.uuid());
    }
}
