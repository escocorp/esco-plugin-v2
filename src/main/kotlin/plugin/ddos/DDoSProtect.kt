package plugin.ddos

import arc.util.Log
import arc.util.Ratekeeper
import mindustry.Vars
import mindustry.gen.Player
import plugin.PVars
import plugin.database.models.PlayerData
import plugin.database.models.putLog
import plugin.discord.Bot
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object DDoSProtect {
    private const val ATTACK_TIMEOUT = 2 * 60 * 1000L

    private const val RATE_SPACING = 1000L
    private const val RATE_AMOUNT = 5
    private const val RATE_ENTRY_TTL = 60 * 1000L

    private val botsKicked = AtomicInteger(0)
    private val lastBotTime = AtomicLong(0L)
    private val attackActive = AtomicBoolean(false)

    private val ipRatekeepers = ConcurrentHashMap<String, Ratekeeper>()
    private val blacklisted = ConcurrentHashMap.newKeySet<String>()

    fun load() {
        try {
            val clazz = Class.forName("plugin.ddos.antiddos.L")

            val instance = clazz.getField("INSTANCE").get(null)
            clazz.getMethod("d").invoke(instance)

            Log.info("AntiDDoS loaded!")
        } catch (e: ClassNotFoundException) {
            Log.info("AntiDDoS not found! Skipping...")
        } catch (e: Exception) {
            Log.err("Error while loading AntiDDoS", e)
        }
    }

    fun checkRatelimit(address: String): Boolean {
        if (blacklisted.contains(address)) return true

        val keeper = ipRatekeepers.computeIfAbsent(address) { Ratekeeper() }
        if (keeper.allow(RATE_SPACING, RATE_AMOUNT)) return false

        if (blacklisted.add(address)) {
            Vars.netServer.admins.blacklistDos(address)
            lastBotTime.set(System.currentTimeMillis())
            if (!attackActive.getAndSet(true)) {
                Bot.sendLog("\n# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
            }
            botsKicked.incrementAndGet()
            Log.info("Blacklisting IP @ due to connection flood", address)
            putLog("ddosprotect", "IP $address blacklisted due to connection flood")
        }
        return true
    }

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

    fun handleBotNoPlayer(address: String): Boolean {
        lastBotTime.set(System.currentTimeMillis())

        if (!attackActive.getAndSet(true)) {
            Bot.sendLog("\n# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
        }

        putLog("ddosprotect", "Player at IP $address detected as bot before connecting!")

        botsKicked.incrementAndGet()

        return true
    }

    fun update() {
        val now = System.currentTimeMillis()

        ipRatekeepers.entries.removeIf { now - it.value.lastTime >= RATE_ENTRY_TTL }

        if (attackActive.get() && now - lastBotTime.get() >= ATTACK_TIMEOUT) {
            attackActive.set(false)
            blacklisted.clear()
            val total = botsKicked.getAndSet(0)
            Bot.sendLog("\n# Bot attack ended✅✅✅✅. Total bots caught: $total")
        }
    }
}
