package plugin.discord

import arc.Core
import arc.func.Cons
import arc.math.Mathf
import arc.struct.Seq
import arc.util.CommandHandler
import arc.util.Log
import arc.util.Strings
import arc.util.Timer
import mindustry.Vars
import mindustry.core.Version
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.maps.Map
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.utils.FileUpload
import plugin.Bundle
import plugin.KVars.buildsBaseUrl
import plugin.KVars.buildsLatestTxtUrl
import plugin.PVars
import plugin.PVars.globalExecutor
import plugin.PVars.version
import plugin.database.Database
import plugin.database.getPlayerData
import plugin.database.models.PlayerData
import plugin.discord.register.CommandType
import plugin.discord.register.DiscordCommand
import plugin.utils.Loader
import plugin.utils.MapPreview
import plugin.utils.Permission
import plugin.utils.download
import plugin.utils.httpGetString
import java.awt.Color
import java.io.File
import java.util.function.Consumer
import java.util.function.Function
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.min

class Commands{
    @DiscordCommand(
        name = "update",
        args = "[ver]",
        desc = "SS",
        type = CommandType.ALL,
        requiredPerm = Permission.editServer
    )
    fun update(arr: Array<String>, ctx: Context){
        globalExecutor.submit {
            try {
                val ver = arr.firstOrNull()?.takeIf { it.isNotBlank() }?.trim()
                    ?: httpGetString(buildsLatestTxtUrl)
                if(version.equals(ver)) {
                    ctx.reply("No new updates! But ok.")
                }
                val modFi = Vars.mods.getMod("plugin").file
                val tmpFi = modFi.parent().child(modFi.name() + ".part")
                download(
                    "$buildsBaseUrl/$ver/plugin.jar",
                    tmpFi.file().toPath()
                )
                modFi.delete()
                Files.move(
                    tmpFi.file().toPath(),
                    modFi.file().toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
                ctx.reply("Successful!\n$version -> $ver")
            } catch (e: Exception) {
                Log.err("Discord update failed", e)
                ctx.reply("ohno ${e.message}")
            }
        }
    }
    @DiscordCommand(
        name = "debug",
        desc = "SS",
        requiredPerm = Permission.editServer
    )
    fun debug(arr: Array<String>, ctx: Context){
        val pool = Database.dataSource.hikariPoolMXBean
        ctx.reply("Restart: ${PVars.needRestart}\nFPS: ${Core.graphics.framesPerSecond}\nHeap: ${Core.app.javaHeap / 1024 / 1024}\nVersion: ${Version.build}\nJava Version: ${Runtime.version()}\n\nDatabase\nTotal: ${pool.totalConnections}\nActive: ${pool.activeConnections}\nIdle: ${pool.idleConnections}\n\nPlugin\nVersion: $version")
    }

    @DiscordCommand(
        name = "restart",
        desc = "SS",
        type = CommandType.ALL,
        requiredPerm = Permission.editServer
    )
    fun restart(arr: Array<String>, ctx: Context){
        sendLog("Restart scheduled by <@" + ctx.message.author.id + ">")
        ctx.message.addReaction(Emoji.fromUnicode("✅")).queue()
        if (Groups.player.isEmpty) {
            Timer.schedule({ Loader.exit() }, 1f)
        }
        PVars.needRestart = true
    }

    @DiscordCommand(
        name = "uploadmap",
        desc = "SS",
        requiredPerm = Permission.editMaps
    )
    fun uploadmap(arr: Array<String>, ctx: Context){
        val msg = ctx.message
        val attachments = Seq<Attachment?>()
        msg.attachments.forEach(Consumer { a: Attachment ->
            if (a.fileExtension == Vars.mapExtension) attachments.add(a)
        })
        if (attachments.size > 4) {
            ctx.reply("Too many maps!")
            return
        }
        attachments.each(Cons { map: Attachment ->
            val file = Vars.customMapDirectory.child(map.fileName)
            if (file.exists()) {
                ctx.reply("File " + file.name() + " already exists!")
                return@Cons
            }
            map.proxy.downloadToFile(file.file()).thenAccept(Consumer { _: File? ->
                ctx.reply("File " + file.name() + " downloaded!")
                Vars.maps.reload()
            }).exceptionally(Function { t: Throwable? ->
                Log.err("Failed to download map!", t)
                null
            })
        })
    }

    @DiscordCommand(
        name = "help",
        desc = "What a dog doing?"
    )
    fun help(args: Array<String>, ctx: Context) {
        val commandsList = PVars.discordCommands.commandList.sort(
            Comparator.comparing(
                Function { commandx: CommandHandler.Command -> commandx.text })
        )
        val commands = StringBuilder()

        for (command in commandsList) {
            commands.append(PVars.discordCommands.getPrefix()).append(command.text)
            if (!command.paramText.isEmpty()) {
                commands.append(" ").append(command.paramText)
            }

            commands.append(" - ").append(command.description).append("\n")
        }
        //reply(m, "Available commands:\n"+commands.toString());
        ctx.reply("Available commands:\n$commands")
    }

    @DiscordCommand(
        name = "status",
        desc = "Check server status"
    )
    fun status(args: Array<String>, ctx: Context) {
        val sb = StringBuilder()
        val embed = EmbedBuilder()
        Groups.player.each { p: Player ->
            getPlayerData(p).ifPresent(Consumer { pd: PlayerData ->
                sb.append("[").append(pd.id).append("] ")
            })
            sb.append(p.plainName()).append("\n")
        }
        embed.addField("Map", Vars.state.map.name(), false)
        embed.addField("TPS", Core.graphics.framesPerSecond.toString(), false)
        embed.addField("Wave", Vars.state.wave.toString(), false)
        embed.addField("Players", sb.toString(), true)
        embed.setColor(Color.green)
        embed.setImage("attachment://minimap.png")
        val image = MapPreview.parseTiles(Vars.world.tiles)
        ctx.message.replyEmbeds(embed.build()).addFiles(FileUpload.fromData(image, "minimap.png")).queue()
    }

    @DiscordCommand(
        name = "link",
        desc = "Link Discord account with server profile",
        args = "<code>"
    )
    fun link(args: Array<String>, ctx: Context) {
        if (args[0].length > 15) return
        if (!PVars.linkCodes.containsKey(args[0])) {
            ctx.reply("Code not found! Are you on server?")
            return
        }
        val player = PVars.linkCodes.get(args[0])
        PVars.linkCodes.remove(args[0])
        getPlayerData(player).ifPresent(Consumer { p: PlayerData ->
            if (p.updateDiscordId(ctx.author.idLong)) ctx.reply("Success!")
            else ctx.reply("Failed")
        })
    }

    @DiscordCommand(
        name = "maps",
        desc = "Show custom maps list",
        args = "[page]"
    )
    fun maps(args: Array<String>, ctx: Context) {
        val mapsList = Vars.maps.customMaps()
        if (mapsList.isEmpty) {
            ctx.reply("No custom maps on server")
            return
        }
        if (args.isNotEmpty() && !Strings.canParseInt(args[0])) {
            ctx.reply("Page must be int!")
            return
        }
        var page = if (args.isNotEmpty()) Strings.parseInt(args[0]) else 1
        val pages = Mathf.ceil(mapsList.size / 16.0f)
        page--
        if (page in 0..<pages) {
            val maps = StringBuilder()

            for (i in 16 * page..<min(16 * (page + 1), mapsList.size)) {
                val map = mapsList.get(i)
                maps.append(Strings.stripColors(map.name())).append("\n")
            }

            ctx.replyEmbed(
                EmbedBuilder()
                    .setColor(Color.green)
                    .setTitle(Strings.format("Maps: @. Page @/@", mapsList.size, page + 1, pages))
                    .addField("", maps.toString(), false)
                    .build()
            )
        } else {
            ctx.reply("Unknown page. Avail. pages 1-$pages")
        }
    }

    @DiscordCommand(
        name = "map",
        desc = "Get detailed information about a map",
        args = "<name...>"
    )
    fun map(args: Array<String>, ctx: Context) {
        val mapsList = Vars.maps.customMaps()
        if (mapsList.isEmpty) {
            ctx.reply("No custom maps on server")
            return
        }
        val map = mapsList.find { m: Map -> m.name().contains(args[0]) }
        if (map == null) {
            ctx.reply("No map found")
            return
        }
        val embed = EmbedBuilder()
        embed.setColor(Color.magenta)
            .setTitle(map.name())
            .setFooter(map.width.toString() + "x" + map.height)
            .setAuthor(map.author())

        // .setDescription(map.description())
        //.setImage("attachment://minimap.png");
        val msg = ctx.channel.sendMessageEmbeds(embed.build())
        msg.addFiles(FileUpload.fromData(map.file.file(), map.file.name()))
        //msg.addFiles(FileUpload.fromData(parseMap(map), "minimap.png"))
        msg.queue()
    }

    @DiscordCommand(
        name = "delmap",
        desc = "Delete a custom map from the server",
        args = "<name...>",
        requiredPerm = Permission.editMaps
    )
    fun delmap(args: Array<String>, ctx: Context) {
        val mapsList = Vars.maps.customMaps()
        if (mapsList.isEmpty) {
            ctx.reply("No custom maps on server")
            return
        }
        val map = mapsList.find { m: Map -> m.name().contains(args[0]) }
        if (map == null) {
            ctx.reply("No map found")
            return
        }
        val embed = EmbedBuilder()
        embed.setColor(Color.red)
            .setTitle("Deleted " + map.name())
            .setFooter(map.width.toString() + "x" + map.height)
            .setAuthor(map.author())

        // .setDescription(map.description())
        //.setImage("attachment://minimap.png");
        val msg = ctx.channel.sendMessageEmbeds(embed.build())
        msg.addFiles(FileUpload.fromData(map.file.file(), map.file.name()))
        //msg.addFiles(FileUpload.fromData(parseMap(map), "minimap.png"))
        map.file.delete()
        Vars.maps.reload()
        msg.queue()
    }

    @DiscordCommand(
        name = "ver",
        desc = "SS",
        type = CommandType.GLOBAL
    )
    fun ver(args: Array<String>, ctx: Context){
        globalExecutor.submit {
            ctx.replyServer("Current: $version  Latest: ${httpGetString(buildsLatestTxtUrl)}")
        }
    }
    @DiscordCommand(
        name = "reloadbundle",
        desc = "SS",
        type = CommandType.GLOBAL,
        requiredPerm = Permission.editServer
    )
    fun reloadBundle(args: Array<String>, ctx: Context){
        Bundle.load()
        ctx.replyServer("IDK, reloaded probably")
    }

    @DiscordCommand(
        name = "gc",
        desc = "SS",
        type = CommandType.GLOBAL,
        requiredPerm = Permission.editServer
    )
    fun collectGarbage(args: Array<String>, ctx: Context){
        val before = Core.app.javaHeap / 1024 / 1024
        System.gc()
        val after = Core.app.javaHeap / 1024 / 1024
        ctx.replyServer("Before: $before\nAfter: $after\nDiff: ${before - after}")
    }
}