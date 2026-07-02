package plugin

import arc.util.CommandHandler
import arc.util.Log
import arc.util.Threads
import mindustry.mod.Plugin
import plugin.commands.CustomHandler
import plugin.commands.register
import plugin.discord.Bot
import plugin.utils.Loader.load

class PPlugin : Plugin() {
    override fun init() {
        instance = this
        load()

        Threads.daemon { Bot.load() }

        Log.info("Plugin successfully loaded!")
    }

    override fun registerServerCommands(handler: CommandHandler) {
        register(handler)
        PVars.serverCommands = handler
        Log.info("Registered @ server commands", handler.commandList.size)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        PVars.clientCommands = CustomHandler(handler)
        Foos.init()
        //ClientCommands.register(clientCommands);
        register(PVars.clientCommands)

        Log.info("Registered @ client commands", handler.commandList.size)
    }

    companion object {
        @JvmField
        var instance: PPlugin? = null
    }
}
