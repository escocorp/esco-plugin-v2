package plugin.discord.listeners

import arc.graphics.Color
import arc.util.CommandHandler
import arc.util.Log
import mindustry.gen.Call
import mindustry.server.ServerControl
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import plugin.PVars
import plugin.discord.Context
import plugin.discord.Bot.reply
import plugin.database.models.Permission
import plugin.utils.hasRole
import plugin.utils.sanitizeDiscordMessage
import java.text.MessageFormat

class MessageListener : ListenerAdapter() {
    // Rate limit: track last message time per user to prevent spam
    private val userCooldowns = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val cooldownMs = 1500L

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        val author = event.author
        val message = event.message
        if (author.isBot || author.isSystem || message.isWebhookMessage) return
        val member = event.member

        val channel = event.getChannel()
        val content = message.contentDisplay

        if (member != null) {
            // RCE protection: block dangerous JS patterns in console command
            val jsSafe = !content.contains("js", true)
            if (jsSafe && channel.id == PVars.consoleChannelStr && member.hasRole(PVars.ownerRoleId)) {
                ServerControl.instance.handleCommandString(content)
            }

            if (channel.id == PVars.serverChannelStr && !content.startsWith(PVars.gamemode.botPrefix)) {
                // Rate limiting: skip if user is on cooldown
                val now = System.currentTimeMillis()
                val lastMsg = userCooldowns.getOrDefault(author.id, 0L)
                if (now - lastMsg < cooldownMs) return
                userCooldowns[author.id] = now

                val username = member.effectiveName
                Log.info("@: @", username, content)
                val colorHex: String = Color(member.colors.primaryRaw).toString()
                val sanitizedContent = sanitizeDiscordMessage(content)
                val mindustryMessage = MessageFormat.format(
                    "[blue]\uE80D[tan][[[#{0}]{1}[tan]][white]: {2}",
                    colorHex,
                    username,
                    sanitizedContent
                ).take(250)
                Call.sendMessage(mindustryMessage)
            }
        }

        if (PVars.discordCommands != null) {
            val response = PVars.discordCommands.handleMessage(content, Context(message, channel, author, false))
            if (response.type == CommandHandler.ResponseType.fewArguments) {
                reply(
                    message,
                    MessageFormat.format(
                        "Too few arguments!\nUsage **{0}{1}** {2}",
                        PVars.gamemode.botPrefix,
                        response.command.text,
                        response.command.paramText
                    )
                )
            } else if (response.type == CommandHandler.ResponseType.manyArguments) {
                reply(
                    message,
                    MessageFormat.format(
                        "Too many arguments!\nUsage **{0}{1}** {2}",
                        PVars.gamemode.botPrefix,
                        response.command.text,
                        response.command.paramText
                    )
                )
            }
        }
        if (PVars.globalCommands != null && content.startsWith("gc!")) {
            val ctx = Context(message, channel, author, true)
            if (!ctx.hasPerm(Permission.EditServer)) return
            PVars.globalCommands.handleMessage(content, ctx)
        }
    }
}