package plugin.utils

/*import plugin.gamemodes.crawlerarena.CrawlerArenaGamemode
import plugin.gamemodes.hexed.HexedGamemode
import plugin.gamemodes.hexed.HexedGamemode.hexedGamemode
import plugin.gamemodes.tdf.TDGamemode*/
import arc.Core
import arc.Events
import arc.util.Http
import arc.util.Log
import arc.util.Timer
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.gen.Groups
import mindustry.net.Administration
import plugin.Bundle
import plugin.Config
import plugin.Gamemode
import plugin.KVars
import plugin.KVars.globalConfigCache
import plugin.KVars.globalScope
import plugin.KVars.messageBuffer
import plugin.PVars
import plugin.PVars.*
import plugin.antigrief.loadGraylist
import plugin.database.BanListener
import plugin.database.Database
import plugin.database.models.Server
import plugin.database.models.getPlayerData
import plugin.database.models.putMessage
import plugin.ddos.DDoSProtect
import plugin.discord.Bot.sendLog
import plugin.events.EscoPluginLoadEvent
import plugin.events.loadEvents
import plugin.maps.EscoMapProvider
import plugin.maps.MapPreview
import plugin.menus.Menu
import plugin.menus.TextMenu
import plugin.menus.loadMenus
import plugin.model.GlobalConfig
import plugin.packets.Packets
import plugin.patches.Patches
import plugin.s3.S3
import plugin.trails.TrailsHandler
import kotlin.system.exitProcess

object Loader {
    @JvmStatic
    fun load() {
        try {
            Config.load()

            if (gamemode == Gamemode.unknown) {
                Log.warn("This server running unknown gamemode!")
            }

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
            TrailsHandler.load()
            DDoSProtect.load()

            loadGlobalConfig()

            if (S3Enabled) {
                S3 = S3(S3BaseUrl, S3AccessKey, S3SecretKey)
            }

            version = getResource("version")?.readString() ?: ""

            Vars.maps.setMapProvider(EscoMapProvider())

            loadMenus()

            Timer.schedule({
                // ipJoins.clear();
                if (joinDemographics.size > 7000) joinDemographics.clear()
            }, 60f, 60f)

            /*
        if(gamemode != Gamemode.hexed && Core.settings.getBool("autorestarted", false)) {
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

        Timer.schedule({
            Events.fire(EscoPluginLoadEvent())
        }, 5f)

        Log.debug("Loader: OK!")
    }

    private fun loadGlobalConfig() {
        Http
            .get(KVars.globalConfigLink)
            .addPluginAuth()
            .timeout(5000)
            .error {
                Log.err("Failed to load global config, fallback to cache", it)
                KVars.globalConfig = readGlobalConfigCache()
            }.submit { resp ->
                val raw = resp.resultAsString
                KVars.globalConfig = objectMapper.readValue(raw, GlobalConfig::class.java)
                globalConfigCache.writeString(raw)
            }
    }

    private fun readGlobalConfigCache(): GlobalConfig? {
        if (!globalConfigCache.exists()) return null
        return try {
            objectMapper.readValue(globalConfigCache.readString(), GlobalConfig::class.java)
        } catch (e: Exception) {
            Log.err("Failed to read cached global config!", e)
            null
        }
    }

    @JvmStatic
    fun loadAfterStart() {
        // ClientCrasher.load();
        // AntiFimoz.load();
        Administration.Config.showConnectMessages.set(true)
        Packets.load()
        /*if(gamemode != Gamemode.hexed) {
            Vars.maps.setMapProvider(PluginMapProvider())
        }*/
    }

    /*fun loadGamemode() {
        when (gamemode) {
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
            if (!Groups.player.isEmpty) Bundle.sendMessage("announce.discord", discordLink)
        }, (15 * 60).toFloat(), (15 * 60).toFloat())
        /*Timer.schedule({
            if (!Groups.player.isEmpty) Bundle.sendMessage("announce.reports", discordLink)
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

        if (lokiLoggingEnabled) {
            Timer.schedule({
                pushLogs()
            }, 0f, (5 * 60).toFloat())
        }

        Timer.schedule({
            DDoSProtect.update()
        }, 30f, 10f)
    }

    fun loadServerId() {
        val serverOpt = Server.getOrCreateServer()
        if (serverOpt != null) {
            serverId = serverOpt.id
        } else {
            Log.err("Сannot create/get server record. Server is unstable")
        }
    }

    fun saveLogs() {
        if (logsBuffer.isEmpty) return
        Log.info("Saving @ logs", logsBuffer.size)

        while (logsBuffer.size > 0) {
            val log = logsBuffer.pop()
            globalScope.launch {
                log.write()
            }
        }
    }

    fun saveMessages() {
        if (messageBuffer.isEmpty) return
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
