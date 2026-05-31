package plugin.discord.register

/**
 * Signals a user-facing error in a Discord command handler.
 *
 * When thrown inside a [DiscordCommand]-annotated function, [DiscordCommandAnnotationProcessor]
 * catches this exception and replies to the invoking message with a red "Error" embed containing
 * [message], instead of propagating it as an unexpected internal failure.
 *
 * @param message The error description to display to the Discord user.
 * @param cause An optional underlying cause for chaining purposes.
 */
class DiscordCommandException(message: String, cause: Throwable? = null) : Exception(message, cause)