package plugin.discord.register

/**
 * Specifies which command handler(s) a [DiscordCommand]-annotated function is registered to.
 */
enum class CommandType {
    /** Registers the command only to the default (guild-specific) command handler. */
    DEFAULT,

    /** Registers the command only to the global command handler. */
    GLOBAL,

    /** Registers the command to both the default and global command handlers. */
    ALL
}