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
                Bundle.sendMessage("vnw.failed")
                cancel()
            }
        }, 30f)
    }

    fun vote(player: Player, d: Int) {
        votes += d
        voted.put(player.ip(), d)

        if (d == 1)
            Bundle.sendMessage("vnw.votedy", player.coloredName(), votes, votesRequired())
        else
            Bundle.sendMessage("vnw.votedn", player.coloredName(), votes, votesRequired())

        checkPass()
    }

    fun checkPass(): Boolean {
        if (votes >= votesRequired()) {
            Vars.logic.runWave()
            cancel()

            Bundle.sendMessage("vnw.pass")
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
