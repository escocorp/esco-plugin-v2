package plugin.commands;

import arc.util.CommandHandler;
import plugin.Bundle;
import plugin.Patches;

public class ServerCommands {
    public static void register(CommandHandler handler) {
        handler.register("reload-bundle", "reload bundle", (a)->{
            Bundle.load();
        });

        handler.register("despawn-units", "Despawn all unused untis", (a)->{
            Patches.despawnUnits();
        });
    }
}
