package plugin.packets;

import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Call;
import mindustry.net.Administration;
import mindustry.net.NetConnection;
import mindustry.gen.Player;
import mindustry.net.Packets;
import plugin.Bundle;
import plugin.Permission;

import static mindustry.Vars.netServer;
import static mindustry.net.Packets.AdminAction.*;
import static plugin.Permission.getPerms;
import static plugin.Utils.parseTime;
import static plugin.database.models.Ban.ban;
import static plugin.database.models.PlayerData.getPlayerData;

public class AdminRequest {
    public static void handle(NetConnection con, AdminRequestCallPacket packet) {
        Player player = con.player;
        Player other = packet.other;
        Packets.AdminAction action = packet.action;
        Object params = packet.params;

        if(!getPerms(player).contains(Permission.admin)) {
            Bundle.sendMessage("noperms", player);
            return;
        }

        if(other == null) {
            return;
        }

        Events.fire(new EventType.AdminRequestEvent(player, other, action));

        switch (action) {
            case wave -> {
                Vars.logic.skipWave();
                Log.info("@ skipped wave!", player);
            }
            case ban -> {
                // ban(other, player, "Touch grass", parseTime("10"));
                player.sendMessage("use /ban");
            }
            case kick -> {
                other.kick(Bundle.get("admin.kicked")+" "+player.coloredName());
            }
            case trace -> {
                String uuid = other.uuid();
                var pdata = getPlayerData(other);

                if(pdata.isPresent())
                    uuid = uuid + " ID: "+pdata.get().id;

                Administration.PlayerInfo stats = netServer.admins.getInfo(other.uuid());
                Administration.TraceInfo info = new Administration.TraceInfo(other.con.address, uuid, other.locale, other.con.modclient, other.con.mobile, stats.timesJoined, stats.timesKicked, stats.ips.toArray(String.class), stats.names.toArray(String.class));
                if(player.con != null){
                    Call.traceInfo(player.con, other, info);
                }else{
                    NetClient.traceInfo(other, info);
                }
            }
            case switchTeam -> {
                if(params instanceof Team team) {
                    other.team(team);
                    other.sendMessage(Bundle.get("team.changed")+" "+team.coloredName());
                }
            }
        }
    }
}
