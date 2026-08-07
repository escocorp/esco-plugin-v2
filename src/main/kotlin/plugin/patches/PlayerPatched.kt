package plugin.patches

import mindustry.game.Team
import mindustry.gen.Player
import plugin.model.getStatus

class PlayerPatched : Player() {
    override fun isSyncHidden(team: Team?): Boolean {
        team ?: return super.isSyncHidden(team)

        return this.getStatus().vanished || super.isSyncHidden(team)
    }
}