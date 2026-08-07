package plugin.votes

import arc.struct.ObjectIntMap
import arc.util.Timer
import mindustry.Vars
import mindustry.gen.Groups
import mindustry.gen.Player
import plugin.Bundle
import plugin.PVars.waveVote
import kotlin.math.max
import kotlin.math.roundToInt

class VoteWave {
    var voted: ObjectIntMap<String> = ObjectIntMap()
    var votes = 0
    var task: Timer.Task? = null

    constructor() {
        task = Timer.schedule({
            if (!checkPass()) {
                Bundle.sendMessage("command.vnw.failed")
                cancel()
            }
        }, 30f)
    }

    fun vote(player: Player, d: Int) {
        votes += d
        voted.put(player.ip(), d)

        if (d == 1)
            Bundle.sendMessage("command.vnw.voted-yes", player.coloredName(), votes, votesRequired())
        else
            Bundle.sendMessage("command.vnw.voted-no", player.coloredName(), votes, votesRequired())

        checkPass()
    }

    fun checkPass(): Boolean {
        if (votes >= votesRequired()) {
            Vars.logic.runWave()
            cancel()

            Bundle.sendMessage("command.vnw.passed")
            return true
        }
        return false
    }

    fun cancel() {
        task?.cancel()
        waveVote = null
    }

    fun votesRequired(): Int {
        return max(1, (Groups.player.size() * 0.75).roundToInt())
    }
}
