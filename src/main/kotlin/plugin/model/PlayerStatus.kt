package plugin.model

import mindustry.gen.Player
import plugin.KVars.frozenTag

data class PlayerStatus(
    var frozen: Boolean = false,
    var vanished: Boolean = false,
    var schemeSizeUser: Boolean = false,
    var foosUser: Boolean = false,
    var agzamModUser: Boolean = false,
    var historyEnabled: Boolean = false,
    var owoAccent: Boolean = false,
    var ohioAccent: Boolean = false,
    var linkCode: String? = null,
    var pingReceived: Boolean = false,
    var fake: Boolean = false,
)

private val playerStatuses = hashMapOf<Player, PlayerStatus>()

fun getOrCreatePlayerStatus(player: Player): PlayerStatus {
    return playerStatuses.getOrPut(player) { PlayerStatus() }
}

fun Player.getStatus(): PlayerStatus {
    return getOrCreatePlayerStatus(this)
}

fun Player.freeze(): PlayerStatus {
    val status = this.getStatus()

    status.frozen = true
    if (!this.name.contains(frozenTag))
        this.name = "$frozenTag ${this.coloredName()}"

    // this.sendMessage("Your frozen!")

    return status
}

fun Player.unfreeze(): PlayerStatus {
    val status = this.getStatus()

    status.frozen = false
    this.name = this.coloredName().replace(frozenTag, "").trim()

    return status
}

fun purgePlayerStatus(player: Player) {
    playerStatuses.remove(player)
}

fun Player.getLinkCode(): String? {
    return this.getStatus().linkCode
}

fun Player.setLinkCode(code: String) {
    this.getStatus().linkCode = code
}

fun Player.removeLinkCode() {
    this.getStatus().linkCode = null
}

fun findPlayerByLinkCode(code: String): Player? {
    return playerStatuses.entries.firstOrNull { it.value.linkCode == code }?.key
}

fun Player.isFake(): Boolean {
    return this.getStatus().fake
}

fun Player.setFake(value: Boolean) {
    this.getStatus().fake = value
}