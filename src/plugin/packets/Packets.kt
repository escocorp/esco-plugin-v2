package plugin.packets

import arc.func.Cons2
import mindustry.Vars
import mindustry.gen.AdminRequestCallPacket
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.gen.SendChatMessageCallPacket
import mindustry.io.JsonIO
import mindustry.net.NetConnection
import plugin.PVars
import plugin.models.getStatus

object Packets {
    fun load() {
        Vars.net.handleServer(
            AdminRequestCallPacket::class.java,
            Cons2 { con: NetConnection?, packet: AdminRequestCallPacket -> AdminRequest.handle(con, packet) })
        Vars.net.handleServer(
            SendChatMessageCallPacket::class.java,
            Cons2 { con: NetConnection?, packet: SendChatMessageCallPacket -> SendChatMessage.handle(con, packet) })

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
