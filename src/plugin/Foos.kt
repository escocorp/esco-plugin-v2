package plugin

import arc.util.Log
import arc.util.Reflect
import arc.util.serialization.Jval
import mindustry.Vars
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.net.Administration.Config
import plugin.PVars.clientCommands
import plugin.commands.CustomHandler
import plugin.database.models.Permission.Companion.getPerms

object Foos {
    private val sb = StringBuilder()
    private val version = "2.1"
    private val transmissions = Config(
        "fooForwardTransmissions",
        "Whether client transmissions (chat, dms, and more) are relayed through the server",
        true
    ) { enableTransmissions() }
    private val commands =
        Config("fooCommandList", "Whether Foo's users are sent the command list on join (for autocomplete)", true)

    /** Called after command creation */
    fun init() {
        Log.info("Loading foos integration")
        /** @since v1 Plugin presence check */
        Vars.netServer.addPacketHandler("fooCheck") { player, _ ->
            Call.clientPacketReliable(player.con, "fooCheck", version)
            enableTransmissions(player)
            sendCommands(player)
        }

        /** @since v1 Client transmission forwarding */
        Vars.netServer.addPacketHandler("fooTransmission") { player, content ->
            if (!transmissions.bool()) return@addPacketHandler
            sb.append(player.id).append(" ").append(content)
            Call.clientPacketReliable("fooTransmission", sb.toString())
            sb.setLength(0) // Reset the builder
        }
    }

    /** @since v2 Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = transmissions.bool().toString()
        if (player != null) Call.clientPacketReliable(player.con, "fooTransmissionEnabled", enabled)
        else Call.clientPacketReliable("fooTransmissionEnabled", enabled)
    }

    /** @since v2 Sends the list of commands to a player */
    private fun sendCommands(player: Player) {
        if (!commands.bool()) return

        val perms = getPerms(player)

        with(Jval.newObject()) {
            add("prefix", Reflect.get<String>(Vars.netServer.clientCommands, "prefix"))
            add("commands", Jval.newObject().apply {
                if (clientCommands != null)
                    clientCommands.commands.each { command: CustomHandler.CommandData ->
                        if (perms.contains(command.permission))
                            add(command.name, command.args)
                    }
            })
            Call.clientPacketReliable(player.con, "commandList", this.toString())
        }
    }
}
