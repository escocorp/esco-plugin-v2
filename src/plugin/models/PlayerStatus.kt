package plugin.models

import mindustry.gen.Player
import plugin.KVars.frozenTag

data class PlayerStatus(var frozen: Boolean = false, var vanished: Boolean = false, var schemeSizeUser: Boolean = false, var foosUser: Boolean = false, var agzamModUser: Boolean = false)

private val playerStatuses = hashMapOf<Player, PlayerStatus>()

fun getOrCreatePlayerStatus(player: Player): PlayerStatus {
    return playerStatuses[player] ?: PlayerStatus()
}

fun Player.getStatus(): PlayerStatus {
    return getOrCreatePlayerStatus(this)
}

fun Player.freeze(): PlayerStatus {
    val status = this.getStatus()

    status.frozen = true
    this.name = "$frozenTag ${this.coloredName()}"

    return status
}

fun purgePlayerStatus(player: Player) {
    playerStatuses.remove(player)
}