package plugin.packets

import arc.util.Log
import arc.util.Timekeeper
import mindustry.core.NetServer
import mindustry.gen.ClientSnapshotCallPacket
import mindustry.net.NetConnection
import plugin.PVars
import plugin.ddos.DDoSProtect
import plugin.discord.Bot

private val accamulator = Timekeeper.ofSeconds(120f)

fun handleClientSnapshot(con: NetConnection, packet: ClientSnapshotCallPacket) {
    val player = con.player
    // Log.info("${packet.viewWidth}x${packet.viewHeight}")
    if(packet.viewWidth == 0f || packet.viewHeight == 0f) {
        if(DDoSProtect.handleBot(player, null))
            return
    }
    NetServer.clientSnapshot(player, packet.snapshotID, packet.unitID, packet.dead, packet.x, packet.y, packet.pointerX, packet.pointerY, packet.rotation, packet.baseRotation, packet.xVelocity, packet.yVelocity, packet.mining, packet.boosting, packet.shooting, packet.chatting, packet.building, packet.selectedBlock, packet.selectedRotation, packet.plans, packet.viewX, packet.viewY, packet.viewWidth, packet.viewHeight);
}