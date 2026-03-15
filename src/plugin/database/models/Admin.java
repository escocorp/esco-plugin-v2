package plugin.database.models;

import arc.struct.Seq;
import plugin.utils.Permission;

import static plugin.database.Database.executeUpdate;

public class Admin {
    // public static ObjectMap<Player, Admin> cache = new ObjectMap<>();
    public int id, playerId, rankId;
    public String rankName;
    public Seq<Permission> perms;
    public boolean hidden;

    public Admin(int id, int playerId, int rankId, String rankName, Seq<Permission> perms, boolean hidden) {
        this.id = id;
        this.playerId = playerId;
        this.rankId = rankId;
        this.rankName = rankName;
        this.perms = perms;
        this.hidden = hidden;
    }

    public boolean updateHidden(boolean hidden) {
        boolean updated = executeUpdate(
                """
                        UPDATE ADMINS SET
                        hidden = ?
                        WHERE id = ?
                        """,
                stmt -> {
                    stmt.setBoolean(1, hidden);
                    stmt.setInt(2, id);
                }
        );
        if (updated)
            this.hidden = hidden;
        return updated;
    }
}
