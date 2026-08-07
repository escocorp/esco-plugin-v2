package plugin.votes

import arc.Events
import arc.struct.ObjectIntMap
import arc.util.Timer
import mindustry.Vars
import mindustry.game.EventType.GameOverEvent
import mindustry.game.Team
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.maps.Map
import plugin.Bundle
import plugin.PVars
import kotlin.math.max
import kotlin.math.roundToInt

class VoteMap(var initiator: Player, var map: Map?) {
    var voted: ObjectIntMap<String> = ObjectIntMap<String>()
    var task: Timer.Task
    var votes: Int = 0

    /*public VoteMap(Player init) {
       this.initiator = init;
       this.task = Timer.schedule(() -> {
           if (!checkPass()) {
               sendMessage("rtv.failed", votes, votesRequired());
               cancel();
           }
       }, 2 * 60);
   }*/
    init {
        this.task = Timer.schedule({
            if (!checkPass()) {
                Bundle.sendMessage("rtv.failed", votes, votesRequired())
                cancel()
            }
        }, (2 * 60).toFloat())
    }

    fun vote(player: Player, d: Int) {
        /*int lastVote = voted.get(player.uuid(), 0) | voted.get(Vars.netServer.admins.getInfo(player.uuid()).lastIP, 0);
        votes -= lastVote;*/

        votes += d
        //voted.put(player.uuid(), d);
        voted.put(Vars.netServer.admins.getInfo(player.uuid()).lastIP, d)
        if (map == null) {
            if (d == 1) Bundle.sendMessage("rtv.votey", player.coloredName(), votes, votesRequired())
            else Bundle.sendMessage("rtv.voten", player.coloredName(), votes, votesRequired())
        } else {
            if (d == 1) Bundle.sendMessage("rtv.voteymap", player.coloredName(), votes, votesRequired(), map!!.name())
            else Bundle.sendMessage("rtv.votenmap", player.coloredName(), votes, votesRequired(), map!!.name())
        }
        checkPass()
    }

    fun checkPass(): Boolean {
        if (votes >= votesRequired()) {
            if (map != null) Vars.maps.setNextMapOverride(map)
            Events.fire(GameOverEvent(Team.derelict))
            Bundle.sendMessage("rtv.pass")

            cancel()
            return true
        }
        return false
    }

    fun cancel() {
        PVars.mapVote = null
        this.task.cancel()
    }

    fun votesRequired(): Int {
        return max(1, (Groups.player.size() * 0.75).roundToInt())
    }
}
