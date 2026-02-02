package plugin.commands;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Timekeeper;
import mindustry.Vars;
import mindustry.core.NetServer;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import plugin.utils.Permission;
import plugin.Bundle;
import java.util.Optional;

import plugin.database.models.*;
import plugin.utils.VotekickSession;

import static plugin.Bundle.sendMessage;
import static plugin.PVars.*;
import static plugin.database.models.Ban.*;
import static plugin.database.models.PlayerData.*;
import static plugin.utils.Permission.getPerms;
import static plugin.utils.Utils.parseTime;
import static plugin.database.models.Ban.ban;

public class ClientCommands {
    public static Seq<Player> rtvVotes = new Seq<>();
    static final int commandsPerPage = 10;
    public static int voteCooldown = 60 * 5;

    public static void register(CustomHandler handler) {
        clientCommands = handler;
        handler.registerCommand("help", "[page]", (args, player)->{
            if (args.length > 0 && !Strings.canParseInt(args[0])) {
                player.sendMessage("[scarlet]\"page\" must be a integer.");
                return;
            }
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
                sendMessage("commands.unknownpage", player);
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
                sendMessage("rtv.error.voted", p);
                return;
            }
            rtvVotes.add(p);
            sendMessage("rtv.playervoted", p.coloredName(), rtvVotes.size+"/"+Math.max(1, (int) Math.ceil(Groups.player.size() * 0.8)));
            updateRtvVotes();
        });

        handler.registerCommand("ban", "<id> <time> <reason...>", Permission.punish, (a, p)->{
            if(Strings.canParseInt(a[0])) {
                int id = Strings.parseInt(a[0]);
                boolean banned = ban(id, p, a[2], parseTime(a[1]));
                if(banned) {
                    getPlayerById(id).ifPresent(player->{
                        Optional<Ban> ban = getBan(player);
                        if(ban.isPresent()) {
                            ban.get().kickPlayer(player);
                        }
                    });
                }
                p.sendMessage(String.valueOf(banned));
            } else {
                p.sendMessage("[scarlet]ID must be int!");
            }
        });

        handler.registerCommand("discord", (a, p)->{
            Call.openURI(p.con, discordLink);
        });

        handler.registerCommand("vote", "<y/n/c>", (arg, player) -> {
            if(currentlyKicking == null){
                // player.sendMessage("[scarlet]Nobody is being voted on.");
                sendMessage("novoteinprogress", player);
            }else{
                if(player.admin && arg[0].equalsIgnoreCase("c")){
                    // Call.sendMessage(Strings.format("[lightgray]Vote canceled by admin[orange] @[lightgray].", player.name));
                    sendMessage("vote.canceledbyadmin", player.coloredName());
                    currentlyKicking.task.cancel();
                    currentlyKicking = null;
                    return;
                }

                if(player.isLocal()){
                    // player.sendMessage("[scarlet]Local players can't vote. Kick the player yourself instead.");
                    sendMessage("vote.local", player);
                    return;
                }

                int sign = switch(arg[0].toLowerCase()){
                    case "y", "yes" -> 1;
                    case "n", "no" -> -1;
                    default -> 0;
                };

                //hosts can vote all they want
                if((currentlyKicking.voted.get(player.uuid(), 2) == sign || currentlyKicking.voted.get(Vars.netServer.admins.getInfo(player.uuid()).lastIP, 2) == sign)){
                    // player.sendMessage(Strings.format("[scarlet]You've already voted @. Sit down.", arg[0].toLowerCase()));
                    sendMessage("vote.alreadyvoted", player, arg[0].toLowerCase());
                    return;
                }

                if(currentlyKicking.target == player){
                    // player.sendMessage("[scarlet]You can't vote on your own trial.");
                    sendMessage("vote.targetisplayer", player);
                    return;
                }

                if(currentlyKicking.target.team() != player.team()){
                    // player.sendMessage("[scarlet]You can't vote for other teams.");
                    sendMessage("vote.otherteam", player);
                    return;
                }

                if(sign == 0){
                    // player.sendMessage("[scarlet]Vote either 'y' (yes) or 'n' (no).");
                    sendMessage("vote.unknownvote", player);
                    return;
                }

                currentlyKicking.vote(player, sign);
            }
        });

        ObjectMap<String, Timekeeper> cooldowns = new ObjectMap<>();
        handler.registerCommand("votekick", "[player] [reason...]", (args, player) -> {
            if(!Administration.Config.enableVotekick.bool()){
                // player.sendMessage("[scarlet]Vote-kick is disabled on this server.");
                sendMessage("votekick.disabled", player);
                return;
            }

            if(Groups.player.size() < 3){
                // player.sendMessage("[scarlet]At least 3 players are needed to start a votekick.");
                sendMessage("votekick.fewplayers", player);
                return;
            }

            if(player.isLocal()){
                // player.sendMessage("[scarlet]Just kick them yourself if you're the host.");
                sendMessage("votekick.localplayer", player);
                return;
            }

            if(currentlyKicking != null){
                // player.sendMessage("[scarlet]A vote is already in progress.");
                sendMessage("votekick.alreadystarted", player);
                return;
            }

            if(args.length == 0){
                StringBuilder builder = new StringBuilder();
                builder.append("[orange]Players to kick: \n");

                Groups.player.each(p -> !p.admin && p.con != null && p != player, p -> {
                    builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id()).append(")\n");
                });
                player.sendMessage(builder.toString());
            }else if(args.length == 1){
                // player.sendMessage("[orange]You need a valid reason to kick the player. Add a reason after the player name.");
                sendMessage("votekick.noreason", player);
            }else{
                Player found;
                if(args[0].length() > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))){
                    int id = Strings.parseInt(args[0].substring(1));
                    found = Groups.player.find(p -> p.id() == id);
                }else{
                    found = Groups.player.find(p -> p.name.equalsIgnoreCase(args[0]));
                }

                if(found != null){
                    if(found == player){
                        // player.sendMessage("[scarlet]You can't vote to kick yourself.");
                        sendMessage("votekick.kickyouself", player);
                    }else if(found.admin){
                        // player.sendMessage("[scarlet]Did you really expect to be able to kick an admin?");
                        sendMessage("votekick.admin", player);
                    }else if(found.isLocal()){
                        // player.sendMessage("[scarlet]Local players cannot be kicked.");
                        sendMessage("votekick.localplayer2", player);
                    }else if(found.team() != player.team()){
                        // player.sendMessage("[scarlet]Only players on your team can be kicked.");
                        sendMessage("votekick.onlyyourteam", player);
                    }else{
                        Timekeeper vtime = cooldowns.get(player.uuid(), () -> new Timekeeper(voteCooldown));

                        if(!vtime.get()){
                            // player.sendMessage("[scarlet]You must wait " + voteCooldown/60 + " minutes between votekicks.");
                            sendMessage("votekick.wait", player, voteCooldown/60);
                            return;
                        }

                        VotekickSession session = new VotekickSession(found, player);
                        session.vote(player, 1);
                        // Call.sendMessage(Strings.format("[lightgray]Reason:[orange] @[lightgray].", args[1]));
                        sendMessage("votekick.start", args[1]);
                        vtime.reset();
                        currentlyKicking = session;
                    }
                }else{
                    // player.sendMessage("[scarlet]No player [orange]'" + args[0] + "'[scarlet] found.");
                    sendMessage("votekick.playernotfound", player, args[0]);
                }
            }
        });
    }

    public static void updateRtvVotes() {
        if (rtvVotes.size >= Math.max(1, (int) Math.ceil(Groups.player.size() * 0.8))) {
            Events.fire(new EventType.GameOverEvent(Team.derelict));
            rtvVotes.clear();
            sendMessage("rtv.pass");
        }
    }
}
