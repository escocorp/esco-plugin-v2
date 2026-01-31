package plugin;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;

import plugin.commands.ClientCommands;
import plugin.commands.CustomHandler;
import plugin.commands.ServerCommands;

public class PPlugin extends Plugin {
    @Override
    public void init() {
        Config.load();
        Bundle.load();
        Patches.load();
        PEvents.load();

        Log.info("Plugin successfully loaded!");
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        ServerCommands.register(handler);
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        ClientCommands.register(new CustomHandler(handler));
    }
}
