package plugin.discord

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import plugin.PVars
import plugin.database.models.Permission
import plugin.database.models.Permission.Companion.getPermsByDiscordId

class Context(var message: Message, var channel: MessageChannelUnion, var author: User, var global: Boolean) {
    var attachments: List<Attachment> = this.message.attachments

    fun hasPerm(perm: Permission): Boolean {
        val has: Boolean = getPermsByDiscordId(author.idLong).contains(perm)
        if (!has) {
            reply("No access! You need " + perm.name)
        }
        return has
    }

    fun reply(content: String?) {
        message.reply("[" + PVars.gamemode.name + "] " + content).queue()
    }

    fun replyServer(content: String?) {
        message.reply("[" + PVars.gamemode.simpleName + "] " + content).queue()
    }

    fun replyEmbed(embed: MessageEmbed) {
        message.reply(MessageCreateData.fromEmbeds(embed)).queue()
    }
}