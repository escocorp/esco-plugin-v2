package plugin.votes

import arc.struct.ObjectIntMap
import arc.util.Timer
import mindustry.Vars
import mindustry.core.NetServer
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.net.Administration.PlayerInfo
import net.dv8tion.jda.api.EmbedBuilder
import plugin.Bundle
import plugin.PVars
import plugin.database.models.ban
import plugin.database.models.getBan
import plugin.database.models.getPlayerId
import plugin.model.freeze
import plugin.model.getStatus
import plugin.model.unfreeze
import java.awt.Color
import java.text.MessageFormat

class VotekickSession(var target: Player, var started: Player, var reason: String) {
    var voted: ObjectIntMap<String?> = ObjectIntMap<String?>()
    var task: Timer.Task
    var votes: Int = 0
    var targetId: Int = 0
    var startedId: Int = 0

    init {
        this.task = Timer.schedule({
            if (!checkPass()) {
                //Call.sendMessage(Strings.format("[lightgray]Vote failed. Not enough votes to kick[orange] @[lightgray].", target.name));
                Bundle.sendMessage("votekick.failed", target.coloredName())
                target.unfreeze()
                cancel()
            }
        }, NetServer.voteDuration)
        var id = getPlayerId(started)
        if (id != null) startedId = id
        id = getPlayerId(target)
        if (id != null) targetId = id

        target.freeze()
    }

    fun vote(player: Player, d: Int) {
        val lastVote = voted.get(player.uuid(), 0) or voted.get(Vars.netServer.admins.getInfo(player.uuid()).lastIP, 0)
        votes -= lastVote

        votes += d
        voted.put(player.uuid(), d)
        voted.put(Vars.netServer.admins.getInfo(player.uuid()).lastIP, d)

        /*Call.sendMessage(Strings.format("[lightgray]@[lightgray] has voted on kicking[orange] @[lightgray].[accent] (@/@)\n[lightgray]Type[orange] /vote <y/n>[] to agree.",
                player.name, target.name, votes, votesRequired()));*/
        Bundle.sendMessage("votekick.voted", player.coloredName(), target.coloredName(), votes, votesRequired())

        checkPass()
    }

    fun checkPass(): Boolean {
        if (votes >= votesRequired()) {
            // Call.sendMessage(Strings.format("[orange]Vote passed.[scarlet] @[orange] will be banned from the server for @ minutes.", target.name, (kickDuration / 60)));
            Bundle.sendMessage("votekick.passed", target.coloredName(), (NetServer.kickDuration / 60))
            //Groups.player.each(p -> p.uuid().equals(target.uuid()), p -> p.kick(Packets.KickReason.vote, kickDuration * 1000));
            cancel()

            sendEmbed(startedId, targetId)
            if (ban(targetId, startedId, "Votekick: " + reason, NetServer.kickDuration.toLong(), "votekick")) {
                getBan(target)?.let({ b -> b.kickPlayer(target) })
            }

            return true
        }
        return false
    }

    fun cancel() {
        task.cancel()
        PVars.currentlyKicking = null
    }

    /**
     * Sends a summary embed to the configured votekicks Discord channel when a votekick passes.
     *
     *
     * The embed contains the initiating player, the targeted player, the final vote count,
     * the kick reason, and the server name. If any administration players participated in the
     * vote, two additional fields are appended listing admins who voted *for* and
     * *against* the kick respectively. Admins are identified by UUID length (24 chars)
     * and deduplicated via a [HashSet] to avoid double-counting IP-keyed entries.
     *
     * @param stId database ID of the player who started the votekick session.
     * @param tId  database ID of the player targeted by the votekick.
     */
    fun sendEmbed(stId: Int, tId: Int) {
        if (PVars.votekicksChannel == null) return

        val embed = EmbedBuilder()

        embed.setColor(Color.red)
            .addField(
                "Votekick", MessageFormat.format(
                    """
                                Started by: [{0}] {1}
                                Target: [{2}] {3}
                                Votes: {4}
                                Reason: {5}
                                Server: {6}
                                
                                """.trimIndent(),
                    stId, started.plainName(),
                    tId, target.plainName(),
                    votes, reason, PVars.gamemode.simpleName
                ), false
            )

        val votedFor = StringBuilder()
        val votedAgainst = StringBuilder()
        val checked = HashSet<PlayerInfo?>()
        for (vote in voted.entries()) {
            if (vote.key!!.length != 24) continue
            val info = Vars.netServer.admins.getInfoOptional(vote.key)
            if (info != null && checked.add(info)) {
                val targetBuilder = if (vote.value == -1) votedAgainst else votedFor
                targetBuilder.append("- ").append(info.lastName).append('\n')
            }
        }
        if (!votedFor.isEmpty()) embed.addField("Voted for", votedFor.toString(), false)
        if (!votedAgainst.isEmpty()) embed.addField("Voted against", votedAgainst.toString(), false)

        PVars.votekicksChannel.sendMessageEmbeds(embed.build()).queue()
    }

    companion object {
        fun votesRequired(): Int {
            return 2 + (if (Groups.player.size() > 4) 1 else 0)
        }
    }
}