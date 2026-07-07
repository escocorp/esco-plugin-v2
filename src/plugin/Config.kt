package plugin

import arc.util.Log
import io.github.cdimascio.dotenv.Dotenv
import plugin.utils.Gamemode
import java.util.*

object Config {
    private val dotenv: Dotenv = Dotenv.configure()
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load()

    fun load() {
        // spec.
        PVars.gamemode = Gamemode.parseGamemode(getEnv("GAMEMODE"))

        // API
        PVars.bundleApi = getEnv("BUNDLE_API", "http://localhost:8080/bundles/")
        PVars.vpnApi = getEnv("VPN_API", "http://localhost:3000/ip/")
        PVars.lokiApi = getEnv("LOKI_API")
        PVars.lokiLoggingEnabled = !PVars.lokiApi.isEmpty()
        PVars.apiAuth = Base64.getEncoder().encodeToString(getEnv("API_AUTH")!!.toByteArray())

        // DB
        PVars.db = getEnv("DB", "mindustry")
        PVars.dbHost = getEnv("DB_HOST", "127.0.0.1")
        PVars.dbPort = getEnv("DB_PORT", "5432")
        PVars.dbUser = getEnv("DB_USER", "plugin")
        PVars.dbPassword = getEnv("DB_PASSWORD")

        // discord
        PVars.botToken = getEnv("TOKEN")
        PVars.serverChannelStr = getEnv("CHANNEL_ID")
        PVars.serverGuildStr = getEnv("GUILD_ID")
        PVars.logsChannelStr = getEnv("LOGS_ID")
        PVars.votekicksChannelStr = getEnv("VOTEKICKS_ID")
        PVars.nsfwChannelStr = getEnv("NSFW_ID")
        PVars.consoleChannelStr = getEnv("CONSOLE_ID")
        PVars.ownerRoleId = getEnv("OWNER_ROLE_ID")

        // S3
        PVars.S3BaseUrl = getEnv("S3_URL")
        PVars.S3AccessKey = getEnv("S3_ACCESS_KEY")
        PVars.S3SecretKey = getEnv("S3_SECRET_KEY")
    }


    private fun getEnv(name: String): String? {
        return getEnv(name, "")
    }

    private fun getEnv(name: String, def: String?): String? {
        var value = dotenv.get(name)
        if (value != null && !value.isEmpty()) {
            // Log.debug(".env @ = @", name, value)
            return value
        }

        value = System.getenv(name)
        if (value != null && !value.isEmpty()) {
            // Log.debug("ENV @ = @", name, value)
            return value
        }

        value = System.getProperty(name)
        if (value != null && !value.isEmpty()) {
            // Log.debug("-D @ = @", name, value)
            return value
        }

        Log.debug("Config '@' not found anywhere, using default: @", name, def)
        return def
    }
}
