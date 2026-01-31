package plugin;

import arc.util.Log;

import static plugin.Gamemode.parseGamemode;

public class Config {
    public static void load() {
        PVars.gamemode = parseGamemode(getEnv("GAMEMODE"));

        PVars.bundleApi = getEnv("BUNDLE_API", "http://localhost:8080/bundles/");

        PVars.db = getEnv("DB", "mindustry");
        PVars.dbHost = getEnv("DB_HOST", "127.0.0.1");
        PVars.dbPort = getEnv("DB_PORT", "5432");
        PVars.dbUser = getEnv("DB_USER", "plugin");
        PVars.dbPassword = getEnv("DB_PASSWORD");
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
