package plugin.packets

import arc.util.Log
import mindustry.Vars
import mindustry.gen.*
import mindustry.io.JsonIO
import mindustry.net.NetConnection
import plugin.PVars
import plugin.model.freeze
import plugin.model.getStatus
import plugin.utils.infoString

object Packets {
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

        /*Vars.netServer.addPacketHandler("griefmod-ye") { p, a ->
            val status = p.getStatus()

            if(status.griefModUser)
                return@addPacketHandler

            val parts = a.split("\u200b")
            val ver = parts[0]
            val mods = parts[1]
                .removePrefix("⟦")
                .removeSuffix("⟧")
                .split("␟")

            var modss = StringBuilder()

            mods.forEach {
                if(modss.length < 150)
                modss.append(it.take(10))
            }

            val uuid = parts[2].take(32)

            Log.warn("GriefMod user detected! ${p.infoString()}/${uuid} $modss")

            if(ver == "456012") {
                p.freeze()
                status.griefModUser = true
            }
        }*/
    }
}
