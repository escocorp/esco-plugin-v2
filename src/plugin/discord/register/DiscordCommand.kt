package plugin.discord.register

import plugin.utils.Permission

/**
 * Marks a function in a command listener class as a Discord bot command.
 *
 * Functions annotated with [DiscordCommand] are discovered and registered automatically
 * by [DiscordCommandAnnotationProcessor.registerCommands]. Each annotated function must
 * accept exactly two parameters: `Array<String>` (parsed command arguments) and
 * [plugin.discord.Context] (the invocation context).
 *
 * @property name The command name used to invoke it in Discord (e.g. `"status"`).
 * @property desc A short human-readable description shown in the help listing.
 * @property args Optional argument signature in arc `CommandHandler` format
 *   (e.g. `"<required> [optional]"`). Leave empty for commands that take no arguments.
 * @property type Which handler(s) this command is registered to; defaults to [CommandType.DEFAULT].
 * @property requiredPerm The minimum [Permission] a caller must hold to execute the command.
 *   Defaults to [Permission.none], meaning anyone can run it.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DiscordCommand(
    val name: String,
    val desc: String,
    val args: String = "",
    val type: CommandType = CommandType.DEFAULT,
    val requiredPerm: Permission = Permission.none
)
