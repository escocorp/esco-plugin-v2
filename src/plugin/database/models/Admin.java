package plugin.database.models;

import arc.struct.Seq;
import mindustry.gen.Player;
import plugin.Permission;
import plugin.database.Database;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static plugin.Permission.getPerms;

public class Admin {
    public int id, playerId, rankId;
    public String rankName;
    public Seq<Permission> perms;

    Admin(int id, int playerId, int rankId, String rankName, Seq<Permission> perms) {
        this.id = id;
        this.playerId = playerId;
        this.rankId = rankId;
        this.rankName = rankName;
        this.perms = perms;
    }

    public static Optional<Admin> getAdmin(Player player) {
        return Database.executeQueryAsync(
                "SELECT \n" +
                        "    COALESCE(ar.name, 'player') AS rank_name,\n" +
                        "    COALESCE(ar.permissions, ARRAY['none']) AS permissions\n" +
                        "FROM players p\n" +
                        "LEFT JOIN admins a ON a.player_id = p.id\n" +
                        "LEFT JOIN admin_ranks ar ON ar.id = a.rank_id\n" +
                        "WHERE p.uuid = ?",
                stmt->stmt.setString(1, player.uuid()),
                rs->{
                    return null;
                }
        );
    }

    public static Admin getAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("id"),
                rs.getInt("player_id"),
                rs.getInt("rank_id"),
                rs.getString("rank_name"),
                getPerms(rs)
        );
    }
}
