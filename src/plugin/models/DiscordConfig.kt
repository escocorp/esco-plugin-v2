package plugin.models

data class DiscordConfig(
    val botToken: String,
    val serverGuildId: String,
    val serverChannelId: String,
    val logsChannelId: String,
    val votekicksChannelId: String,
    val nsfwChannelId: String,
    val consoleChannelId: String,
    val ownerRoleId: String,
    val inviteLink: String = "https://discord.gg/KfusjwYFDx"
)