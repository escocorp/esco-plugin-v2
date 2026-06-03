package plugin;

import arc.util.Log;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Base64;

import static plugin.utils.Gamemode.parseGamemode;

/**
 * Configuration manager for the plugin.
 * Handles loading environment variables and system properties to populate plugin settings.
 */
public class Config {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    /**
     * Loads configuration settings from various sources into {@link PVars}.
     * This method initializes gamemode, API endpoints, database credentials, and Discord bot settings.
     */
    public static void load() {
        // spec.
        PVars.gamemode = parseGamemode(getEnv("GAMEMODE"));

        // API
        PVars.bundleApi = getEnv("BUNDLE_API", "http://localhost:8080/bundles/");
        PVars.vpnApi = getEnv("VPN_API", "http://localhost:3000/ip/");
        PVars.lokiApi = Config.getEnv("LOKI_API");
        PVars.lokiLoggingEnabled = !PVars.lokiApi.isEmpty();
        PVars.apiAuth = Base64.getEncoder().encodeToString(Config.getEnv("API_AUTH").getBytes());

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
        PVars.logsChannelStr = getEnv("LOGS_ID");
        PVars.votekicksChannelStr = getEnv("VOTEKICKS_ID");
        PVars.roundsChannelStr = getEnv("ROUNDS_ID");
        PVars.parrotChannelStr = getEnv("PARROT_ID");
        PVars.nsfwChannelStr = getEnv("NSFW_ID");
    }


    /**
     * Retrieves an environment variable with a default empty string.
     *
     * @param name The name of the environment variable to retrieve.
     * @return The value of the environment variable, or an empty string if not found.
     */
    private static String getEnv(String name) {
        return getEnv(name, "");
    }

    /**
     * Retrieves an environment variable from .env file, system environment, or system properties.
     *
     * @param name The name of the environment variable to retrieve.
     * @param def The default value to return if the variable is not found.
     * @return The value of the environment variable if present; otherwise, the default value.
     */
    private static String getEnv(String name, String def) {
        String value = dotenv.get(name);
        if (value != null && !value.isEmpty()) {
            Log.debug(".env @ = @", name, value);
            return value;
        }

        value = System.getenv(name);
        if (value != null && !value.isEmpty()) {
            Log.debug("ENV @ = @", name, value);
            return value;
        }

        value = System.getProperty(name);
        if (value != null && !value.isEmpty()) {
            Log.debug("-D @ = @", name, value);
            return value;
        }

        Log.debug("Config '@' not found anywhere, using default: @", name, def);
        return def;
    }
}
