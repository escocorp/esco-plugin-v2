package plugin.utils

import arc.Core
import arc.Events
import arc.util.Log
import arc.util.Timer
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.gen.Groups
import mindustry.net.Administration
import plugin.Bundle
import plugin.Config
import plugin.KVars.globalScope
import plugin.KVars.messageBuffer
import plugin.PVars
import plugin.PVars.joinDemographics
import plugin.antigrief.loadGraylist
import plugin.database.BanListener
import plugin.database.Database
import plugin.database.models.getPlayerData
import plugin.database.models.Server
import plugin.database.models.putMessage
import plugin.ddos.DDoSProtect
import plugin.discord.Bot.sendLog
import plugin.events.EscoPluginLoadEvent
import plugin.events.loadEvents
import plugin.maps.EscoMapProvider
import plugin.maps.MapPreview
/*import plugin.gamemodes.crawlerarena.CrawlerArenaGamemode
import plugin.gamemodes.hexed.HexedGamemode
import plugin.gamemodes.hexed.HexedGamemode.hexedGamemode
import plugin.gamemodes.tdf.TDGamemode*/
import plugin.menus.Menu
import plugin.menus.TextMenu
import plugin.menus.loadMenus
import plugin.packets.Packets
import plugin.patches.Patches
import plugin.s3.S3
import kotlin.system.exitProcess

object Loader {

    @JvmStatic
    fun load() {
        try {
            Config.load()
            Database.load()
            Bundle.load()
            Patches.load()
            loadEvents()
            MapPreview.loadColors()
            loadServerId()
            loadTimers()
            // loadGamemode()
            BanListener.load()
            Menu.load()
            TextMenu.load()
            loadGraylist()
            PVars.S3 = S3(PVars.S3BaseUrl, PVars.S3AccessKey, PVars.S3SecretKey)

            PVars.version = getResource("version")?.readString() ?: ""

            Vars.maps.setMapProvider(EscoMapProvider())

            loadMenus()

            Timer.schedule({
                // ipJoins.clear();
                if (joinDemographics.size > 7000) joinDemographics.clear()
            }, 60f, 60f)

            /*
        if(PVars.gamemode != Gamemode.hexed && Core.settings.getBool("autorestarted", false)) {
            if(state.isGame) {
                Vars.net.closeServer()
                ServerControl.instance.cancelPlayTask()
                state.set(GameState.State.menu)
                Log.info("Stopped server.")
            }

            loadSave("autorestart")

            Core.settings.put("autorestarted", false)
        }*/

        } catch (e: Exception) {
            Log.err(e)
        }

        Events.fire(EscoPluginLoadEvent())

        Log.debug("Loader: OK!")
    }

    @JvmStatic
    fun loadAfterStart() {
        // ClientCrasher.load();
        // AntiFimoz.load();
        Administration.Config.showConnectMessages.set(false)
        Packets.load()
        /*if(PVars.gamemode != Gamemode.hexed) {
            Vars.maps.setMapProvider(PluginMapProvider())
        }*/
    }

    /*fun loadGamemode() {
        when (PVars.gamemode) {
            Gamemode.tdefense -> TDGamemode.load()
            Gamemode.hexed -> {
                hexedGamemode = HexedGamemode()
                hexedGamemode.init()
                hexedGamemode.registerServerCommands(serverCommands)
            }

            Gamemode.crawlerArena -> CrawlerArenaGamemode.init()
            else -> {}
        }
    }*/

    fun loadTimers() {
        Timer.schedule({
            if (!Groups.player.isEmpty) Bundle.sendMessage("advertise.discord", PVars.discordLink)
        }, (15 * 60).toFloat(), (15 * 60).toFloat())
        /*Timer.schedule({
            if (!Groups.player.isEmpty) Bundle.sendMessage("advertise.reports", PVars.discordLink)
        }, (15 * 60).toFloat(), (35 * 60).toFloat())*/
        Timer.schedule({
            Groups.player.each { p ->
                globalScope.launch {
                    getPlayerData(p)?.updateStats(p, false)
                }
            }
        }, (15 * 60).toFloat(), (15 * 60).toFloat())

        Timer.schedule({
            saveLogs()
            saveMessages()
        }, (5 * 60).toFloat(), (5 * 60).toFloat())

        if (PVars.lokiLoggingEnabled)
            Timer.schedule({
                pushLogs()
            }, 0f, (5 * 60).toFloat())

        Timer.schedule({
            DDoSProtect.update()
        }, 30f, 10f)
    }

    fun loadServerId() {
        val serverOpt = Server.getOrCreateServer()
        if (serverOpt != null) PVars.serverId = serverOpt.id
        else Log.err("Сannot create/get server record. Server is unstable")
    }

    fun saveLogs() {
        if(PVars.logsBuffer.isEmpty) return
        Log.info("Saving @ logs", PVars.logsBuffer.size)

        while (PVars.logsBuffer.size > 0) {
            val log = PVars.logsBuffer.pop()
            globalScope.launch {
                log.write()
            }
        }
    }

    fun saveMessages() {
        if(messageBuffer.isEmpty) return
        Log.info("Saving @ messages", messageBuffer.size)
        while (messageBuffer.size > 0) {
            val message = messageBuffer.pop()
            globalScope.launch {
                putMessage(message.playerId, message.unformatted, message.formatted, message.timestamp)
            }
        }
    }

    @JvmStatic
    fun exit() {
        Log.info("Exiting server, please wait...")
        sendLog("Exiting server")
        save("autorestart")
        Core.settings.put("autorestarted", true)
        Core.settings.manualSave()
        saveLogs()
        Timer.schedule({
            exitProcess(0)
        }, 5f)
    }
}
