package plugin.packets

import arc.util.Log
import arc.util.Timekeeper
import mindustry.core.NetServer
import mindustry.gen.ClientSnapshotCallPacket
import mindustry.net.NetConnection
import plugin.ddos.DDoSProtect
import plugin.model.getStatus
import plugin.utils.infoString

fun handleClientSnapshot(
    con: NetConnection,
    packet: ClientSnapshotCallPacket,
) {
    val player = con.player
    // Log.info("${packet.viewWidth}x${packet.viewHeight}")
    if (packet.viewWidth == 0f && packet.viewHeight == 0f) {
        Log.warn("Detected bot player by snapshot view method @ [@] (@)", player.plainName(), player.uuid(), player.ip())
        DDoSProtect.handleBot(player, null)
    }

    /*if(player.unit() != null && packet.unitID != player.unit().id && player.getStatus().badSnapshots.allow(5000, 12)) {
        Log.warn("Detected bot player by snapshot unit id method! ${player.infoString()}")
        DDoSProtect.handleBot(player, null)
    }*/

    NetServer.clientSnapshot(
        player,
        packet.snapshotID,
        packet.unitID,
        packet.dead,
        packet.x,
        packet.y,
        packet.pointerX,
        packet.pointerY,
        packet.rotation,
        packet.baseRotation,
        packet.xVelocity,
        packet.yVelocity,
        packet.mining,
        packet.boosting,
        packet.shooting,
        packet.chatting,
        packet.building,
        packet.selectedBlock,
        packet.selectedRotation,
        packet.plans,
        packet.viewX,
        packet.viewY,
        packet.viewWidth,
        packet.viewHeight,
    )
}
