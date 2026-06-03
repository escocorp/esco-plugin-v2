package plugin;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Threads;
import mindustry.mod.Plugin;
import plugin.commands.ClientCommandsKt;
import plugin.commands.CustomHandler;
import plugin.commands.ServerCommandsKt;
import plugin.discord.BotKt;
import plugin.utils.Loader;

import static plugin.PVars.clientCommands;
import static plugin.PVars.serverCommands;

/**
 * Main entry point for the Esco Plugin.
 * Responsible for initializing the plugin, registering server and client commands,
 * and starting the Discord bot.
 */
public class PPlugin extends Plugin {
    /**
     * Static reference to the main plugin instance.
     */
    public static PPlugin mainClass;

    /**
     * Initializes the plugin.
     * Loads core plugin components and starts the Discord bot in a daemon thread.
     */
    @Override
    public void init() {
        mainClass = this;
        Loader.load();

        Threads.daemon(BotKt::load);

        Log.info("Plugin successfully loaded!");
    }

    /**
     * Registers server-side commands.
     *
     * @param handler The command handler provided by Mindustry to register commands.
     */
    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommandsKt.register(handler);
        serverCommands = handler;
        Log.info("Registered @ server commands", handler.getCommandList().size);
    }

    /**
     * Registers client-side commands.
     *
     * @param handler The command handler provided by Mindustry to register commands.
     */
    @Override
    public void registerClientCommands(CommandHandler handler) {
        clientCommands = new CustomHandler(handler);
        Foos.Companion.init();
        //ClientCommands.register(clientCommands);
        ClientCommandsKt.register(clientCommands);

        Log.info("Registered @ client commands", handler.getCommandList().size);
    }
}
