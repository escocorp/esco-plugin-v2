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
import java.util.function.Consumer

object BanListener {
    private
    var failedTimes: Int = 0
    private
    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun load() {
        executor.submit {
            try {
                dataSource!!.getConnection().use { con ->
                    val pgCon = con.unwrap(PGConnection::class.java)
                    con.createStatement().use { st ->
                        st.execute("LISTEN new_ban")
                    }
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
                Log.err(e)
                sendLog(e.message)
                //if (failedTimes < 5) {
                load()
                failedTimes += 1
                //}
            }
        }
    }
}