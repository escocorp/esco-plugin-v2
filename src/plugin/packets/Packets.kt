package plugin.packets

import mindustry.Vars
import mindustry.gen.AdminRequestCallPacket
import mindustry.gen.Call
import mindustry.gen.ClientSnapshotCallPacket
import mindustry.gen.Player
import mindustry.gen.SendChatMessageCallPacket
import mindustry.io.JsonIO
import mindustry.net.NetConnection
import plugin.PVars
import plugin.model.getStatus

object Packets {
    val lastPingMap = mutableMapOf<String, Long>()

    fun load() {
        Vars.net.handleServer(
            AdminRequestCallPacket::class.java
        ) { con: NetConnection, packet: AdminRequestCallPacket -> AdminRequest.handle(con, packet) }
        Vars.net.handleServer(
            SendChatMessageCallPacket::class.java
        ) { con: NetConnection, packet: SendChatMessageCallPacket -> SendChatMessage.handle(con, packet) }

        Vars.net.handleServer(
            ClientSnapshotCallPacket::class.java
        ) { con, packet ->
            handleClientSnapshot(con, packet)
        }

        loadCustom()
    }

    private fun loadCustom() {
        // scheme size integration
        Vars.netServer.addPacketHandler("MySubtitle") { target: Player, args: String ->
            PVars.SSUsers.put(target.id, args)
            Call.clientPacketReliable("Subtitles", JsonIO.write(PVars.SSUsers))

            target.getStatus().schemeSizeUser = true
        }
        // agzam mod
        Vars.netServer.addBinaryPacketHandler("agzam4.cmd-sug") { player, bs ->
            player.getStatus().agzamModUser = true
        }
    }
}
