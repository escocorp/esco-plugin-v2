package plugin.patches

import arc.Core
import arc.Events
import arc.files.Fi
import arc.func.Cons
import arc.func.Func
import arc.util.ColorCodes
import arc.util.Log
import arc.util.Timer
import mindustry.Vars
import mindustry.ctype.Content
import mindustry.game.EventType.GameOverEvent
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.net.Administration
import mindustry.type.UnitType
import mindustry.world.blocks.storage.CoreBlock
import plugin.Bundle
import plugin.PVars
import plugin.ai.DumbAI
import plugin.discord.Bot.sendConsoleMessage
import plugin.utils.Gamemode
import plugin.utils.addLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Patches {
    internal var tags: Array<String?> =
        arrayOf("&lc&fb[DEBUG]&fr", "&lb&fb[INFO]&fr", "&ly&fb[WARN]&fr", "&lr&fb[ERROR]", "")
    internal var dateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val logFolder: Fi = Core.settings.getDataDirectory().child("logs/")
    var currentLogFile: Fi? = null

    fun load() {
        if (PVars.gamemode == Gamemode.sandbox) {
            Vars.content.units().each(Cons { u: UnitType ->
                if (!u.hidden) u.controller = Func { _: Unit -> DumbAI() }
            })

            Timer.schedule({ despawnUnits() }, (30 * 60).toFloat(), (30 * 60).toFloat())

            Timer.schedule({
                Call.sendMessage("[stat]Time to change map! Save your buildings.")
                Timer.schedule({
                    Events.fire(GameOverEvent(Team.derelict))
                }, 10f)
            }, (60 * 60 * 6).toFloat(), (60 * 60 * 6).toFloat())

            Vars.content.each { content: Content ->
                if (content is CoreBlock) {
                    content.health = 999999999
                }
            }
        }
        loadLogger()

        Core.app.removeListener(Vars.netServer)
        Vars.netServer.dispose()
        Vars.netServer = NetServerPatched()
        Core.app.addListener(Vars.netServer)
    }

    fun despawnUnits() {
        Log.info("Time to despawn unused units!")
        Bundle.sendMessage("unitdespawn")
        Groups.unit.each(Cons { u: Unit ->
            if (!u.controller().toString().lowercase(Locale.getDefault()).startsWith("player")) u.kill()
        })
    }

    fun loadLogger() {
        Log.logger = Log.LogHandler { level1: Log.LogLevel, text: String ->
            var text = text
            //err has red text instead of reset.
            if (level1 == Log.LogLevel.err) text =
                text.replace(ColorCodes.reset, ColorCodes.lightRed + ColorCodes.bold)

            val result =
                ColorCodes.bold + ColorCodes.lightBlack + "[" + dateTime.format(LocalDateTime.now()) + "] " + ColorCodes.reset + Log.format(
                    tags[level1.ordinal] + " " + text + "&fr"
                )
            println(result)
            addLog(level1.name, text)

            val cleanText = "[" + dateTime.format(LocalDateTime.now()) + "] " + Log.formatColors(
                tags[level1.ordinal] + " " + text + "&fr",
                false
            )

            sendConsoleMessage(cleanText.replace("\u001B\\[[;\\d]*m".toRegex(), ""))
            if (Administration.Config.logging.bool()) {
                logToFile(cleanText)
            }
        }
    }

    fun logToFile(text: String) {
        var text = text
        if (currentLogFile != null && currentLogFile!!.length() > Administration.Config.maxLogLength.num()) {
            currentLogFile!!.writeString(
                "[End of log file. Date: " + dateTime.format(LocalDateTime.now()) + "]\n",
                true
            )
            currentLogFile = null
        }

        for (value in ColorCodes.values) {
            text = text.replace(value, "")
        }

        if (currentLogFile == null) {
            var i = 0
            while (logFolder.child("log-$i.txt").length() >= Administration.Config.maxLogLength.num()) {
                i++
            }

            currentLogFile = logFolder.child("log-$i.txt")
        }

        currentLogFile!!.writeString(text + "\n", true)
    }
}
