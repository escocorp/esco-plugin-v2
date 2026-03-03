package plugin.antigrief;

import mindustry.gen.Player;
import plugin.Bundle;
import plugin.database.models.PlayerData;

import java.util.Optional;

import static plugin.PVars.discordLink;
import static plugin.database.Database.executeQueryAsync;
import static plugin.database.models.Log.putLog;
import static plugin.database.models.PlayerData.getPlayerData;
import static plugin.database.models.PlayerData.getPlayerId;

public class Graylist {
    public static void apply(Player p, String isp, PlayerData pd) {
        if(!(isGraylisted(isp) && pd.discordId == null)) return;
        p.kick(Bundle.get("kick.graylisted", p.locale, discordLink), 0);
        putLog(pd.id, "graylist", "Player graylisted by IP "+p.ip());
    }

    public static boolean isGraylisted(String isp) {
        return executeQueryAsync(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM graylist WHERE isp ILIKE ?
                        )
                        """,
                stmt->stmt.setString(1, isp),
                rs->rs.getBoolean("exists")
        ).orElse(false);
    }
}