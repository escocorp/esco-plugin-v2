package plugin.packets;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.gen.SendChatMessageCallPacket;
import mindustry.net.Administration;
import mindustry.net.NetConnection;
import mindustry.net.Packets;
import mindustry.net.ValidateException;

import static mindustry.Vars.*;
import static mindustry.Vars.headless;
import static mindustry.Vars.netServer;
import static mindustry.gen.Call.sendMessage;

public class SendChatMessage {
    public static void handle(NetConnection con, SendChatMessageCallPacket packet) {
        Player player = con.player;
        String message = packet.message;

        //do not receive chat messages from clients that are too young or not registered
        if(net.server() && player != null && player.con != null && (Time.timeSinceMillis(player.con.connectTime) < 500 || !player.con.hasConnected || !player.isAdded())) return;

        //detect and kick for foul play
        if(player != null && player.con != null && !player.con.chatRate.allow(2000, Administration.Config.chatSpamLimit.num())){
            player.con.kick(Packets.KickReason.kick);
            netServer.admins.blacklistDos(player.con.address);
            return;
        }

        if(message == null) return;

        if(message.length() > maxTextLength){
            throw new ValidateException(player, "Player has sent a message above the text limit.");
        }

        message = message.replace("\n", "");

        Events.fire(new EventType.PlayerChatEvent(player, message));

        //log commands before they are handled
        if(message.startsWith(netServer.clientCommands.getPrefix()) && Administration.Config.logCommands.bool()){
            //log with brackets
            Log.info("<&fi@: @&fr>", "&lk" + player.plainName(), "&lw" + message);
        }

        //check if it's a command
        CommandHandler.CommandResponse response = netServer.clientCommands.handleMessage(message, player);
        Log.debug("@ @", message, response.type);
        if(response.type == CommandHandler.ResponseType.noCommand){ //no command to handle
            message = netServer.admins.filterMessage(player, message);
            //suppress chat message if it's filtered out
            if(message == null){
                return;
            }

            //special case; graphical server needs to see its message
            if(!headless){
                sendMessage(netServer.chatFormatter.format(player, message), message, player);
            }

            //server console logging
            Log.info("&fi@: @", "&lc" + player.plainName(), "&lw" + message);

            //invoke event for all clients but also locally
            //this is required so other clients get the correct name even if they don't know who's sending it yet
            Call.sendMessage(netServer.chatFormatter.format(player, message), message, player);
        }else{

            //a command was sent, now get the output
            if(response.type != CommandHandler.ResponseType.valid){
                String text = netServer.invalidHandler.handle(player, response);
                if(text != null){
                    player.sendMessage(text);
                }
            }
        }
    }
}
