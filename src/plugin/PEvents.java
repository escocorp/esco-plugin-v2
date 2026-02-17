package plugin;

import arc.Core;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import arc.Events;
import mindustry.gen.*;
import mindustry.net.Administration;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import plugin.database.models.Admin;
import plugin.database.models.Ban;
import plugin.database.models.PlayerData;
import plugin.database.models.PlayerStats;
import plugin.discord.Bot;
import plugin.history.History;
import plugin.history.HistoryStack;
import plugin.packets.Packets;
import plugin.utils.Loader;
import plugin.utils.Permission;
import arc.util.Strings;
import mindustry.net.Administration.ActionType;

import java.util.Optional;

import static plugin.PVars.*;
import static plugin.database.models.Ban.ban;
import static plugin.database.models.Log.putLog;
import static plugin.database.models.PlayerData.getPlayerId;
import static plugin.discord.Bot.sendLog;
import static plugin.utils.Gamemode.sandbox;
import static plugin.utils.Permission.getPerms;
import static plugin.utils.Permission.seqToString;
import static plugin.utils.Utils.*;
import static plugin.database.models.Admin.getAdmin;
import static plugin.database.models.Ban.getBan;
import static plugin.database.models.PlayerData.getPlayerData;

public class PEvents {
    public static void load() {
        Events.on(EventType.PlayerConnect.class, (e)->{ // pre-connect
            Player player = e.player;

            Optional<PlayerData> pdataOpt = PlayerData.getOrCreatePlayerData(player);
            if(pdataOpt.isEmpty()) {
                player.kick("[scarlet]Failed to create player!", 0);
                return;
            }
            PlayerData pd = pdataOpt.get();
            pd.getUsid().ifPresent(u->{
                if(!u.equals(player.usid())) {
                    putLog(pd.id, "system", "Possible account thief");
                    player.kick("Failed to get player data.\nUsid in database is different from current!\nPlease contact us.\nDiscord: " + discordLink, 0);
                    sendLog("Possible account thief! Usid: "+player.usid()+" Database: "+u);
                }
            });

            isAnon(player.ip(), ()->{
                if(pd.discordId == null) {
                    putLog(pd.id, "system", "Detected using vpn or proxy.");
                    player.kick("You detected by [pink]AntiVPN[] system\nTry re-connect and disable vpn/proxy\nOr try linking your discord by /link\nDiscord: " + discordLink);
                }
            });

            Optional<Ban> banOpt = getBan(player);
            if(banOpt.isPresent()) {
                Ban ban = banOpt.get();
                ban.kickPlayer(player);
                putLog(pd.id, "system", "Ban "+ban.id+" hit!");
                sendLog("New ban hit!\nReason: "+ban.reason+"\n"+"ID: "+ban.id+"\nNickname: "+player.plainName().replace("@", ""));
                return;
            }

            getAdmin(player).ifPresent(a->{
                if(a.perms.contains(Permission.admin) && !a.hidden)
                    player.admin(true);
                if(a.perms.size > 1)
                    player.sendMessage("Your permissions "+seqToString(a.perms));
            });

            if(mapVote != null)
                mapVote.checkPass();

            // putLog(pd.id, "system", "All checks finished");
        });

        Events.on(EventType.PlayerJoin.class, (e)->{ // full connect
            Player player = e.player;

            Optional<PlayerData> pdOpt = getPlayerData(player);
            if(pdOpt.isEmpty()) {
                player.kick("[scarlet]Failed to create player!");
                return;
            }
            PlayerData pd = pdOpt.get();

            PlayerStats.setJoinTime(player);
            PlayerStats.getPlayerStats(player);

            Bundle.sendMessage("messages.join", String.valueOf(pd.id), player.coloredName());
            putLog(pd.id, "event", "Player joined!");

            Log.info("[@] Player @ joined [@]", pd.id, player.plainName(), player.uuid());
            Bot.sendJoinMessage(player, pd.id);

            Call.clientPacketReliable(player.con, "SendMeSubtitle", player == null ? null : String.valueOf(player.id));

            // simple bot check
            Timer.schedule(()->{
                if(player.con.isConnected() && player.con.lastReceivedClientSnapshot == -1) {
                    putLog(pd.id, "system", "Player detected as bot");
                    player.kick("[scarlet]Try reconnect\nDiscord " + discordLink, 0);
                }
            }, 1);

        });

        Events.on(EventType.PlayerLeave.class, (e)->{
            Player player = e.player;

            if (player != null/* how? */) SSUsers.remove(player.id);

            Optional<PlayerData> pdOpt = getPlayerData(player);
            if(pdOpt.isPresent()) {
                PlayerData pd = pdOpt.get();
                Bundle.sendMessage("messages.leave", String.valueOf(pd.id), player.coloredName());
                Log.info("[@] Player @ left [@]", pd.id, player.plainName(), player.uuid());
                Bot.sendLeaveMessage(player, pd.id);
                putLog(pd.id, "event", "Player disconnected");
            }
            if(currentlyKicking != null && currentlyKicking.target.equals(player)) {
                ban(currentlyKicking.targetId, currentlyKicking.startedId, "AutoBan: Leave during votekick", 2*60*60);
                currentlyKicking.cancel();
            }

            purgeData(player);

            /*if(rtvVotes.contains(player)) {
                rtvVotes.remove(player);
                Bundle.sendMessage("rtv.playerleft", rtvVotes.size+"/"+Math.max(1, (int) Math.round(Groups.player.size() * 0.8)));
            }*/
            if(mapVote != null)
                mapVote.checkPass();

            if(needRestart) {
                Loader.exit();
            }
        });

        Events.on(EventType.PlayerChatEvent.class, (e)->{
            Player player = e.player;
            String message = e.message;

            getPlayerData(player).ifPresent(pd->{
                putLog(pd.id, "event", "Player sent message "+message);
            });

	        if(!message.startsWith("/"))
            	Bot.sendServerMessage(("`"+player.plainName()+": "+stripFoo(Strings.stripColors(message))+"`").replace("@", ""));
        });

        Events.on(EventType.ServerLoadEvent.class, (e)->{
            Administration.Config.showConnectMessages.set(false);
            Packets.load();
            Vars.netServer.admins.addActionFilter(a->{
                Administration.ActionType type = a.type;
                Player player = a.player;

                Optional<PlayerData> pdOpt = getPlayerData(player);
                if(pdOpt.isEmpty()) {
                    return false;
                }
                PlayerData pd = pdOpt.get();

                if(type == Administration.ActionType.buildSelect || type == Administration.ActionType.configure)
                    if(a.tile.block() instanceof LogicBlock && a.config instanceof byte[] b) {
                        String code = decompress(b);
                        if(code.isEmpty()) return true;
                        if(countWords("radar", code) > 25) {
                            Bundle.sendMessage("logic.antilag", player);
                            putLog(pd.id, "system", "Player possible building lag machines!");
                            return false;
                        }
                    }

                // putLog(a, pd);
                return true;
            });
        });

        Events.on(EventType.BlockBuildEndEvent.class, (e)->{
            if(e.tile == null || e.unit == null)
                return;

            Player player = e.unit.getPlayer();

            if(player != null)
                PlayerStats.getPlayerStats(player).ifPresent(s->{
                    if(e.breaking)
                        s.adjBlocksBroken();
                    else
                        s.adjBlocksBuild();
                });

            if(e.breaking) return;

            Unit unit = e.unit;
            Tile tile = e.tile;
            Integer pid = null;
            String name = null;
            if(player != null) {
                pid = getPlayerId(player).orElse(null);
                name = player.coloredName();
            }

            History.write(tile.pos(), name, pid, ActionType.buildSelect, tile.block(), unit.type());
        });

        Events.on(EventType.BlockBuildBeginEvent.class, (e)-> {
            if(e.tile == null || e.unit == null || !e.breaking)
                return;
            Player player = e.unit.getPlayer();
            Unit unit = e.unit;
            Tile tile = e.tile;
            Integer pid = null;
            String name = null;
            if(player != null) {
                pid = getPlayerId(player).orElse(null);
                name = player.coloredName();
            }

            History.write(tile.pos(), name, pid, ActionType.breakBlock, tile.block(), unit.type());
        });

        Events.on(EventType.BuildRotateEvent.class, (e)->{
            if(e.build == null || e.unit == null || e.unit.getPlayer() == null)
                return;
            Player player = e.unit.getPlayer();
            Building build = e.build;
            Integer pid = null;
            String name = null;
            if(player != null) {
                pid = getPlayerId(player).orElse(null);
                name = player.coloredName();
            }

            History.write(build.pos(), name, pid, ActionType.rotate, build.block, null);
        });

        Events.on(EventType.ConfigEvent.class, (e)->{
            if(e.player == null || e.tile == null)
                return;
            Player player = e.player;
            Building build = e.tile;
            Integer pid = getPlayerId(player).orElse(null);
            String name = player.coloredName();

            History.write(build.pos(), name, pid, ActionType.configure, build.block, null);
        });

        Events.on(EventType.GameOverEvent.class, (e)->{
            if(mapVote != null)
                mapVote.cancel();
            History.clear();
        });

        Events.on(EventType.WorldLoadEvent.class, (e)->{
            if(gamemode == sandbox)
                Timer.schedule(()->{
                    Vars.state.rules.unitDamageMultiplier = 0;
                    Vars.state.rules.blockDamageMultiplier = 0;
                    Vars.state.rules.unitHealthMultiplier = 0.1f;
                    Vars.state.rules.blockHealthMultiplier = 0.1f;
                }, 1);
        });

        Events.on(EventType.TapEvent.class, (e)->{
            if(e.player == null || e.tile == null || !historyPlayers.contains(e.player))
                return;
            Call.setHudText(e.player.con, History.getMessage(e.tile.pos()));
        });
    }

    public static void purgeData(Player p) {
        Permission.cache.remove(p);
        PlayerData.cache.remove(p);
        Admin.cache.remove(p);
        PlayerStats.purge(p);
        historyPlayers.remove(p);

        if(linkCodes.containsValue(p, false))
            linkCodes.forEach(e->{
                if(e.value.equals(p))
                    Core.app.post(()->linkCodes.remove(e.key));
            });
    }
}
