package plugin.utils;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Player;
import plugin.database.Database;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Enum defining the available permissions within the plugin.
 * Provides utilities to parse, retrieve, and manage permissions for players.
 */
public enum Permission {
    none,
    test,
    admin,
    punish,
    editMaps,
    votekickImmune,
    editServer,
    vanish,
    artv,
    replays;

    /**
     * Cache for storing permissions of players to reduce database queries.
     */
    public static final ObjectMap<Player, Seq<Permission>> cache = new ObjectMap<>();
    // public static final Permission[] all = values();

    /**
     * Parses an array of permission names into a sequence of {@link Permission} objects.
     *
     * @param perms Array of permission names as strings.
     * @return A sequence of parsed permissions.
     */
    public static Seq<Permission> parsePerms(String[] perms) {
        Seq<Permission> rperms = new Seq<>();
        for (String pname : perms)
            rperms.add(parsePerm(pname));
        return rperms;
    }

    /**
     * Parses a single permission name into a {@link Permission} object.
     *
     * @param name The name of the permission.
     * @return The matching {@link Permission}, or {@code none} if no match is found.
     */
    public static Permission parsePerm(String name) {
        for (Permission p : values())
            if (p.name().contains(name)) return p;
        return none;
    }

    /**
     * Retrieves the permissions for a specific player, using the cache if available.
     *
     * @param p The player to retrieve permissions for.
     * @return A sequence of the player's permissions.
     */
    public static Seq<Permission> getPerms(Player p) {
        if (cache.containsKey(p))
            return cache.get(p);

        Optional<Seq<Permission>> r = Database.executeQueryAsync(
                "SELECT ar.permissions\n" +
                        "        FROM players p\n" +
                        "        LEFT JOIN admins a ON a.player_id = p.id\n" +
                        "        LEFT JOIN admin_ranks ar ON ar.id = a.rank_id\n" +
                        "        WHERE p.uuid = ?",
                stmt -> stmt.setString(1, p.uuid()),
                Permission::getPerms
        );

        Seq<Permission> seq = r.orElseGet(() -> Seq.with(none));
        if (!seq.contains(none))
            seq.add(none);
        cache.put(p, seq);

        return seq;
    }

    /**
     * Retrieves permissions based on a Discord user ID.
     *
     * @param discordId The Discord ID of the user.
     * @return A sequence of permissions associated with that Discord ID.
     */
    public static Seq<Permission> getPermsByDiscordId(Long discordId) {
        Optional<Seq<Permission>> r = Database.executeQueryAsync(
                "SELECT ar.permissions\n" +
                        "        FROM players p\n" +
                        "        LEFT JOIN admins a ON a.player_id = p.id\n" +
                        "        LEFT JOIN admin_ranks ar ON ar.id = a.rank_id\n" +
                        "        WHERE p.discord_id = ?",
                stmt -> stmt.setLong(1, discordId),
                Permission::getPerms
        );

        Seq<Permission> seq = r.orElseGet(() -> Seq.with(none));
        if (!seq.contains(none))
            seq.add(none);

        return seq;
    }

    /**
     * Extracts permissions from a SQL ResultSet.
     *
     * @param rs The SQL result set.
     * @return A sequence of parsed permissions.
     * @throws SQLException If a database access error occurs.
     */
    public static Seq<Permission> getPerms(ResultSet rs) throws SQLException {
        Array sqlArray = rs.getArray("permissions");
        String[] perms;

        if (sqlArray != null) {
            perms = (String[]) sqlArray.getArray();
        } else {
            perms = new String[]{"none"};
        }

        return Permission.parsePerms(perms);
    }

    /**
     * Converts a sequence of permissions into a space-separated string.
     *
     * @param s The sequence of permissions to convert.
     * @return A string representation of the permissions.
     */
    public static String seqToString(Seq<Permission> s) {
        StringBuilder sb = new StringBuilder();
        s.each(p -> sb.append(p.name() + " "));
        return sb.toString();
    }
}
