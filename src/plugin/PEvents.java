package plugin;

import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import arc.Events;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.world.blocks.logic.LogicBlock;
import plugin.database.models.Ban;
import plugin.database.models.PlayerData;
import plugin.packets.Packets;

import java.util.Optional;

import static plugin.Permission.getPerms;
import static plugin.Utils.countWords;
import static plugin.Utils.decompress;
import static plugin.commands.ClientCommands.updateRtvVotes;
import static plugin.database.models.Ban.getBan;
import static plugin.commands.ClientCommands.rtvVotes;

public class PEvents {
    public static void load() {
        Events.on(EventType.PlayerConnect.class, (e)->{ // pre-connect
            Player player = e.player;

            Optional<PlayerData> pdata = PlayerData.getOrCreatePlayerData(player);
            if(pdata.isEmpty()) {
                player.kick("[scarlet]Failed to create player!", 0);
                return;
            }

            Optional<Ban> ban = getBan(player);
            if(ban.isPresent()) {
                ban.get().kickPlayer(player);
                return;
            }

            player.admin(getPerms(player).contains(Permission.admin));
        });

        Events.on(EventType.PlayerJoin.class, (e)->{ // full connect
            Player player = e.player;
            // simple bot check
            Timer.schedule(()->{
                if(player.con.isConnected() && player.con.lastReceivedClientSnapshot == -1)
                    player.kick("[scarlet]Try reconnect", 0);
            }, 1);

        });

        Events.on(EventType.PlayerLeave.class, (e)->{
            Player player = e.player;

            purgeCache(player);

            if(rtvVotes.contains(player)) {
                rtvVotes.remove(player);
                Bundle.sendMessage("rtv.playerleft", rtvVotes.size+"/"+Math.max(1, (int) Math.ceil(Groups.player.size() * 0.8)));
                updateRtvVotes();
            }
        });

        Events.on(EventType.ServerLoadEvent.class, (e)->{
            Packets.load();
            Vars.netServer.admins.addActionFilter(a->{
                Administration.ActionType type = a.type;
                Player player = a.player;
                if(type == Administration.ActionType.buildSelect || type == Administration.ActionType.configure)
                    if(a.tile.block() instanceof LogicBlock && a.config instanceof byte[] b) {
                        String code = decompress(b);
                        if(code.isEmpty()) return true;
                        if(countWords("radar", code) > 15) {
                            Bundle.sendMessage("logic.antilag", player);
                            return false;
                        }
                    }
                return true;
            });
        });

    }

    public static void purgeCache(Player p) {
        Permission.cache.remove(p);
        PlayerData.cache.remove(p);
    }
}
