package plugin.commands;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandRunner;
import mindustry.gen.Player;

import plugin.Bundle;
import plugin.utils.Permission;

public class CustomHandler {
    final CommandHandler handler;
    public final Seq<CommandData> commands = new Seq<CommandData>();

    public CustomHandler(CommandHandler handler) {
        this.handler = handler;
        registerPseudoCommands();
    }

    public void registerCommand(String name, CommandRunner<Player> runner) {
        registerCommand(name, "", Permission.none, runner);
    }

    public void registerCommand(String name, String args, CommandRunner<Player> runner) {
        registerCommand(name, args, Permission.none, runner);
    }

    public void registerCommand(String name, String args, Permission perm, CommandRunner<Player> runner) {
        // CommandData cd = new CommandData(name, args, perm);
        commands.add(new CommandData(name, args, perm));

        handler.<Player>register(name, args, "", (a, p)->{
            if(!Permission.getPerms(p).contains(perm)) {
                Bundle.sendMessage("noperms", p);
                return;
            }
            runner.accept(a, p);
        });
    }

    public void registerPseudoCommands() {
        addPseudoCommand("t", "<message...>");
        addPseudoCommand("a", "<message...>", Permission.admin);
        addPseudoCommand("votekick", "[player] [reason...]");
        addPseudoCommand("vote", "<y/n/c>");
        addPseudoCommand("sync", "");
    }

    public void addPseudoCommand(String name, String args) {
        addPseudoCommand(name, args, Permission.none);
    }

    public void addPseudoCommand(String name, String args, Permission perm) {
        commands.add(new CommandData(name, args, perm));
    }

    public class CommandData {
        public String name, args;
        public Permission permission;

        CommandData(String name, String args, Permission perm) {
            this.name = name;
            this.args = args;
            this.permission = perm;
        }
    }
}
