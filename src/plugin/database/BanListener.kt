package plugin.database

import arc.util.Log
import arc.util.Strings
import arc.util.Time
import mindustry.gen.Player
import org.postgresql.PGConnection
import plugin.Bundle
import plugin.database.Database.dataSource
import plugin.database.models.getBan
import plugin.database.models.getPlayerById
import plugin.discord.Bot.sendLog
import plugin.utils.formatTime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object BanListener {
    private const val MAX_RETRIES = 10
    private const val RETRY_DELAY_MS = 5000L

    private val failedTimes = AtomicInteger(0)
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun load() {
        executor.submit {
            try {
                failedTimes.set(0)
                while (true) {
                    try {
                        dataSource?.getConnection()?.use { con ->
                            val pgCon = con.unwrap(PGConnection::class.java)
                            con.createStatement().use { st ->
                                st.execute("LISTEN new_ban")
                            }
                            // Reset retry counter on successful connection
                            failedTimes.set(0)
                            while (true) {
                                val notifications = pgCon.notifications

                                if (notifications != null) for (notify in notifications) {
                                    val payload = notify.parameter
                                    if (!Strings.canParseInt(payload)) continue
                                    val i = Strings.parseInt(payload)
                                    getBan(i)?.let { ban ->
                                        getPlayerById(ban.playerId).ifPresent(Consumer { p: Player? ->
                                            Bundle.sendMessage(
                                                "advertise.banned",
                                                ban.playerId,
                                                p!!.coloredName(),
                                                if (ban.unbanTime == null) "never" else formatTime((ban.unbanTime.toEpochMilli() - Time.millis()) / 1000),
                                                ban.reason
                                            )
                                            ban.kickPlayer(p)
                                        })
                                    }
                                }

                                Thread.sleep(1500)
                            }
                        }
                    } catch (e: Exception) {
                        val retries = failedTimes.incrementAndGet()
                        Log.err("BanListener connection error (attempt $retries/$MAX_RETRIES)", e)
                        sendLog("BanListener connection error: ${e.message} (attempt $retries/$MAX_RETRIES)")

                        if (retries >= MAX_RETRIES) {
                            Log.err("BanListener exhausted retries, giving up")
                            sendLog("BanListener exhausted retries, giving up")
                            return@submit
                        }

                        Thread.sleep(RETRY_DELAY_MS)
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                Log.err("BanListener interrupted", e)
            }
        }
    }
}
