package plugin.utils;

import arc.struct.ObjectIntMap;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.Packets;

import static mindustry.core.NetServer.kickDuration;
import static mindustry.core.NetServer.voteDuration;
import static plugin.PVars.*;

public class VotekickSession{
    public Player target;
    public ObjectIntMap<String> voted = new ObjectIntMap<>();
    public Timer.Task task;
    public int votes;
    public Player started;

    public VotekickSession(Player target, Player started){
        this.target = target;
        this.started = started;
        this.task = Timer.schedule(() -> {
            if(!checkPass()){
                Call.sendMessage(Strings.format("[lightgray]Vote failed. Not enough votes to kick[orange] @[lightgray].", target.name));
                currentlyKicking = null;
                task.cancel();
            }
        }, voteDuration);
    }

    public void vote(Player player, int d){
        int lastVote = voted.get(player.uuid(), 0) | voted.get(Vars.netServer.admins.getInfo(player.uuid()).lastIP, 0);
        votes -= lastVote;

        votes += d;
        voted.put(player.uuid(), d);
        voted.put(Vars.netServer.admins.getInfo(player.uuid()).lastIP, d);

        Call.sendMessage(Strings.format("[lightgray]@[lightgray] has voted on kicking[orange] @[lightgray].[accent] (@/@)\n[lightgray]Type[orange] /vote <y/n>[] to agree.",
                player.name, target.name, votes, votesRequired()));

        checkPass();
    }

    public boolean checkPass(){
        if(votes >= votesRequired()){
            Call.sendMessage(Strings.format("[orange]Vote passed.[scarlet] @[orange] will be banned from the server for @ minutes.", target.name, (kickDuration / 60)));
            Groups.player.each(p -> p.uuid().equals(target.uuid()), p -> p.kick(Packets.KickReason.vote, kickDuration * 1000));
            currentlyKicking = null;
            task.cancel();
            return true;
        }
        return false;
    }

    public static int votesRequired(){
        return 2 + (Groups.player.size() > 4 ? 1 : 0);
    }
}