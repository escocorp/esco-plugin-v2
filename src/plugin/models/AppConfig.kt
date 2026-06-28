package plugin.models

import plugin.utils.Gamemode

data class AppConfig(
    var serverId: Int,
    val version: String = "unknown",
    val lokiLoggingEnabled: Boolean = false,
    val gamemode: Gamemode,

    val api: ApiConfig,
    val discord: DiscordConfig,
    val database: DatabaseConfig,
    val hub: HubConfig
)