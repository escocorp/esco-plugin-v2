package plugin.ddos

import mindustry.gen.Player
import plugin.PVars
import plugin.database.models.PlayerData
import plugin.database.models.putLog
import plugin.discord.Bot

object DDoSProtect {
    private const val ATTACK_TIMEOUT = 2 * 60 * 1000L

    val lastPing = mutableMapOf<String, Long>()

    private var botsKicked = 0
    private var lastBotTime = 0L
    private var attackActive = false

    fun handleBot(player: Player, pd: PlayerData?): Boolean {
        player.kick("[scarlet]Try reconnect\nDiscord " + PVars.discordLink, 5)

        lastBotTime = System.currentTimeMillis()

        if (!attackActive) {
            attackActive = true
            Bot.sendLog("# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
        }

        if(pd == null) {
            putLog("ddosprotect", "Player ${player.uuid()} detected as bot!")
        } else {
            putLog(pd.id, "ddosprotect", "Player ${player.uuid()} detected as bot!")
        }

        botsKicked++

        return true
    }

    fun update() {
        if (attackActive &&
            System.currentTimeMillis() - lastBotTime >= ATTACK_TIMEOUT) {

            attackActive = false
            Bot.sendLog("Bot attack ended✅✅✅✅. Total bots caught: $botsKicked")

            botsKicked = 0
        }
    }
}