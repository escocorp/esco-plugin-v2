package plugin.commands;

import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import plugin.Permission;
import plugin.Bundle;

import static plugin.Permission.getPerms;
import static plugin.Utils.parseTime;
import static plugin.database.models.Ban.ban;

public class ClientCommands {
    public static Seq<Player> rtvVotes = new Seq<>();
    final int commandsPerPage = 10;

    public static void register(CustomHandler handler) {
        handler.registerCommand("help", "[page]", (args, player)->{
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                player.sendMessage("[scarlet]\"page\" must be a integer.");
                return;
            }

            int commandsPerPage = 10;
            int page = args.length > 0 ? Strings.parseInt(args[0]) - 1 : 0;

            int availableCommands = 0;

            StringBuilder result = new StringBuilder();
            Seq<Permission> perms = getPerms(player);

            Seq<String> pages = new Seq<>();

            for (int i = 0; i < handler.commands.size; i++) {
                CustomHandler.CommandData c = handler.commands.get(i);

                if (!perms.contains(c.permission)) continue;

                if (availableCommands >= commandsPerPage) {
                    pages.add(result.toString());
                    result.setLength(0);
                    availableCommands = 0;
                }

                String req = "commands." + c.name + ".description";
                String desc = Bundle.get(req);
                if(desc.equals(req))
                    desc = Bundle.get("commands.nodesc");

                availableCommands++;
                result.append("[orange]/")
                        .append(c.name)
                        .append(" [white]")
                        .append(c.args)
                        .append(" - ")
                        .append(desc)
                        .append("\n");
            }

            if(result.length() != 0)
                pages.add(result.toString());

            if((page + 1) > pages.size || page < 0) {
                Bundle.sendMessage("commands.unknownpage", player);
                return;
            }
            String resp = "[orange]-- Commands Page "+(page+1)+"/"+pages.size+" --\n\n" + pages.get(page);
            player.sendMessage(resp);
        });
        handler.registerCommand("test", "", Permission.test, (a, p)->{
            p.sendMessage("[green]Ok!");
        });

        handler.registerCommand("rtv", "", (a, p)->{
            if(rtvVotes.contains(p)) {
                Bundle.sendMessage("rtv.error.voted", p);
                return;
            }
            rtvVotes.add(p);
            Bundle.sendMessage("rtv.playervoted", p.coloredName(), rtvVotes.size+"/"+Math.max(1, (int) Math.ceil(Groups.player.size() * 0.8)));
            updateRtvVotes();
        });

        handler.registerCommand("ban", "<id> <time> <reason...>", Permission.punish, (a, p)->{
            boolean banned = ban(Strings.parseInt(a[0]), p, a[2], parseTime(a[1]));
            p.sendMessage(String.valueOf(banned));
        });
    }

    public static void updateRtvVotes() {
        if (rtvVotes.size >= Math.max(1, (int) Math.ceil(Groups.player.size() * 0.8))) {
            Events.fire(new EventType.GameOverEvent(Team.derelict));
            rtvVotes.clear();
            Bundle.sendMessage("rtv.pass");
        }
    }
}
