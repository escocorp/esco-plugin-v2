package plugin.ddos

import mindustry.gen.Player
import plugin.PVars
import plugin.database.models.PlayerData
import plugin.database.models.putLog
import plugin.discord.Bot
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean

object DDoSProtect {
    private const val ATTACK_TIMEOUT = 2 * 60 * 1000L

    val lastPing = ConcurrentHashMap<String, Long>()

    private val botsKicked = AtomicInteger(0)
    private val lastBotTime = AtomicLong(0L)
    private val attackActive = AtomicBoolean(false)

    fun handleBot(player: Player, pd: PlayerData?): Boolean {
        player.kick("[scarlet]Try reconnect\nDiscord " + PVars.discordLink, 5)

        lastBotTime.set(System.currentTimeMillis())

        if (!attackActive.getAndSet(true)) {
            Bot.sendLog("\n# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
        }

        if(pd == null) {
            putLog("ddosprotect", "Player ${player.uuid()} detected as bot!")
        } else {
            putLog(pd.id, "ddosprotect", "Player ${player.uuid()} detected as bot!")
        }

        botsKicked.incrementAndGet()

        return true
    }

    fun update() {
        if (attackActive.get() &&
            System.currentTimeMillis() - lastBotTime.get() >= ATTACK_TIMEOUT) {

            attackActive.set(false)
            val total = botsKicked.getAndSet(0)
            Bot.sendLog("\n# Bot attack ended✅✅✅✅. Total bots caught: $total")
        }
    }
}
