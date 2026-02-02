package plugin;

import arc.util.Log;

import static plugin.utils.Gamemode.parseGamemode;

public class Config {
    public static void load() {
        // spec.
        PVars.gamemode = parseGamemode(getEnv("GAMEMODE"));

        // API
        PVars.bundleApi = getEnv("BUNDLE_API", "http://localhost:8080/bundles/");
        PVars.vpnApi = getEnv("VPN_API", "http://localhost:3000/ip/");

        // DB
        PVars.db = getEnv("DB", "mindustry");
        PVars.dbHost = getEnv("DB_HOST", "127.0.0.1");
        PVars.dbPort = getEnv("DB_PORT", "5432");
        PVars.dbUser = getEnv("DB_USER", "plugin");
        PVars.dbPassword = getEnv("DB_PASSWORD");

        // discord
        PVars.botToken = getEnv("TOKEN");
        PVars.serverChannelStr = getEnv("CHANNEL_ID");
        PVars.serverGuildStr = getEnv("GUILD_ID");
    }

    private static String getEnv(String name) {
        return getEnv(name, "");
    }

    private static String getEnv(String name, String def) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            Log.debug("Environment variable '@' is not set, using default: @", name, def);
            return def;
        }
        return value;
    }
}
