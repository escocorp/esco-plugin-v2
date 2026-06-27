package plugin.database.models

import arc.util.Time
import mindustry.gen.Player
import plugin.Bundle
import plugin.PVars.discordLink
import plugin.utils.formatTime
import java.text.MessageFormat
import java.time.Instant

class Ban(
    val id: Int,
    val playerId: Int,
    val adminId: Int,
    var active: Boolean,
    val banTime: Instant,
    val unbanTime: Instant?,
    val reason: String
) {

    fun kickPlayer(player: Player) {
        val time = if (unbanTime == null) {
            "Never (perm-ban)"
        } else {
            formatTime((unbanTime.toEpochMilli() - Time.millis()) / 1000)
        }

        player.kick(
            MessageFormat.format(
                Bundle.get("banned"),
                reason,
                time,
                discordLink,
                id
            ),
            0
        )
    }
}