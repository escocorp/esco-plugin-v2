package plugin

import arc.util.CommandHandler
import arc.util.Log
import arc.util.Threads
import mindustry.mod.Plugin
import plugin.commands.CustomHandler
import plugin.commands.register
import plugin.utils.Loader.load

class PPlugin : Plugin() {
    val instance: PPlugin = this

    override fun init() {
        load()

        Threads.daemon { load() }

        Log.info("Plugin successfully loaded!")
    }

    override fun registerServerCommands(handler: CommandHandler) {
        register(handler)
        serverCommands = handler
        Log.info("Registered @ server commands", handler.getCommandList().size)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        clientCommands = CustomHandler(handler)
        Foos.init()
        //ClientCommands.register(clientCommands);
        register(clientCommands)

        Log.info("Registered @ client commands", handler.getCommandList().size)
    }
}