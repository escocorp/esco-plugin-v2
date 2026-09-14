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

    private const val RATE_SPACING = 5000L
    private const val RATE_AMOUNT = 5
    private const val RATE_ENTRY_TTL = 60 * 1000L

    private const val UDP_RATE_SPACING = 5000L
    private const val UDP_RATE_AMOUNT = 5

    private val botsKicked = AtomicInteger(0)
    private val lastBotTime = AtomicLong(0L)
    private val attackActive = AtomicBoolean(false)

    private val ipRatekeepers = ConcurrentHashMap<String, Ratekeeper>()
    private val blacklisted = ConcurrentHashMap.newKeySet<String>()

    private val udpRatekeepers = ConcurrentHashMap<String, Ratekeeper>()
    private val blacklistedUdp = ConcurrentHashMap.newKeySet<String>()

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

    /**
     * Checks whether a connection must be rejected as flood.
     *
     * [udpAddress] is the real origin of the connection: SOCKS proxies relay the TCP
     * stream but not the UDP one, so a proxy-spread bot attack shares a single UDP
     * address while every TCP address differs.
     *
     * @return `true` if the connection is flood and must be dropped
     */
    fun checkRatelimit(
        address: String,
        udpAddress: String,
    ): Boolean {
        if (blacklisted.contains(address)) return true

        if (udpAddress != address) {
            if (blacklistedUdp.contains(udpAddress)) {
                blacklistAddress(address)
                return true
            }

            val udpKeeper = udpRatekeepers.computeIfAbsent(udpAddress) { Ratekeeper() }
            if (!udpKeeper.allow(UDP_RATE_SPACING, UDP_RATE_AMOUNT)) {
                if (blacklistedUdp.add(udpAddress)) {
                    Log.info("Blacklisting UDP origin @ due to connection flood", udpAddress)
                    putLog("ddosprotect", "UDP origin $udpAddress blacklisted due to connection flood")
                }
                blacklistAddress(address)
                return true
            }
        }

        val keeper = ipRatekeepers.computeIfAbsent(address) { Ratekeeper() }
        if (keeper.allow(RATE_SPACING, RATE_AMOUNT)) return false

        blacklistAddress(address)
        return true
    }

    private fun blacklistAddress(address: String) {
        if (!blacklisted.add(address)) return

        Vars.netServer.admins.blacklistDos(address)
        lastBotTime.set(System.currentTimeMillis())
        if (!attackActive.getAndSet(true)) {
            Bot.sendLog("\n# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
        }
        botsKicked.incrementAndGet()
        Log.info("Blacklisting IP @ due to connection flood", address)
        putLog("ddosprotect", "IP $address blacklisted due to connection flood")
    }

    fun isAttackActive(): Boolean = attackActive.get()

    fun handleBot(
        player: Player,
        pd: PlayerData?,
    ): Boolean {
        player.kick("Maybe you're a bot, try reconnect in 5s\nDiscord " + PVars.discordLink, 5)

        lastBotTime.set(System.currentTimeMillis())

        if (!attackActive.getAndSet(true)) {
            Bot.sendLog("\n# ⚠⚠⚠ Possible bot attack started!⚠⚠⚠")
        }

        if (pd == null) {
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
        udpRatekeepers.entries.removeIf { now - it.value.lastTime >= RATE_ENTRY_TTL }

        if (attackActive.get() && now - lastBotTime.get() >= ATTACK_TIMEOUT) {
            attackActive.set(false)
            blacklisted.clear()
            blacklistedUdp.clear()
            val total = botsKicked.getAndSet(0)
            Bot.sendLog("\n# Bot attack ended✅✅✅✅. Total bots caught: $total")
        }
    }
}
