package plugin.commands

import arc.Core
import arc.Events
import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.CommandHandler.CommandRunner
import arc.util.Log.debug
import arc.util.Strings
import arc.util.Time
import arc.util.Timekeeper
import arc.util.Timer
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.graphics.Pal
import mindustry.net.Administration
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import plugin.Bundle
import plugin.KVars.globalScope
import plugin.PVars
import plugin.PVars.S3Enabled
import plugin.PVars.discordOauthBaseUrl
import plugin.PVars.hubIp
import plugin.PVars.hubPort
import plugin.database.models.*
import plugin.discord.ButtonIds.testId
/*import plugin.gamemodes.crawlerarena.CVars
import plugin.gamemodes.crawlerarena.CrawlerArenaGamemode
import plugin.gamemodes.hexed.Hex
import plugin.gamemodes.hexed.HexedGamemode.hexedGamemode*/
import plugin.history.History
import plugin.menus.Menu
import plugin.menus.ScrollableMenu
import plugin.menus.ScrollableTextMenu
import plugin.menus.showShop
import plugin.model.getLinkCode
import plugin.model.getStatus
import plugin.model.setLinkCode
import plugin.replays.saveReplay
import plugin.trails.TrailsHandler
import plugin.utils.*
import plugin.votes.VoteMap
import plugin.votes.VoteWave
import plugin.votes.VotekickSession
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

const val commandsPerPage = 10
var voteCooldown = 60 * 5

fun register(handler: CustomHandler) {
    TrailsHandler.registerCommands(handler)

    handler.registerCommand("owo", "") { _: Array<String>, player: Player ->
        val status = player.getStatus()
        status.owoAccent = !status.owoAccent
        Bundle.sendMessage(if (status.owoAccent) "command.owo.on" else "command.owo.off", player)
    }

    handler.registerCommand("ohio", "") { _: Array<String>, player: Player ->
        val status = player.getStatus()
        status.ohioAccent = !status.ohioAccent
        Bundle.sendMessage(if (status.ohioAccent) "command.ohio.on" else "command.ohio.off", player)
    }

    handler.registerCommand("settings", "") { _: Array<String>, player: Player ->
        val pd = player.getData()
        if(pd == null) {
            Bundle.sendMessage("command.no-player-data", player)
            return@registerCommand
        }
        val menu = Menu("@menu.settings.title", "@menu.settings.message")

        menu.add("Show welcome menu\n${parseBool(pd.prefs.showWelcomeMenu)}") {
            Call.infoMessage(player.con, "From ${parseBool(pd.prefs.showWelcomeMenu, colored = true)} -> ${parseBool(!pd.prefs.showWelcomeMenu, colored = true)}")
            pd.prefs.showWelcomeMenu = !pd.prefs.showWelcomeMenu
        }

        menu.add("${Bundle.get("menu.settings.owo", player.locale)}\n${parseBool(pd.prefs.owoAccent)}") { p ->
            pd.prefs.owoAccent = !pd.prefs.owoAccent
            p.getStatus().owoAccent = pd.prefs.owoAccent
            globalScope.launch { pd.updatePrefs() }
            Bundle.sendMessage(if (pd.prefs.owoAccent) "command.owo.on" else "command.owo.off", p)
        }

        menu.add("${Bundle.get("menu.settings.ohio", player.locale)}\n${parseBool(pd.prefs.ohioAccent)}") { p ->
            pd.prefs.ohioAccent = !pd.prefs.ohioAccent
            p.getStatus().ohioAccent = pd.prefs.ohioAccent
            globalScope.launch { pd.updatePrefs() }
            Bundle.sendMessage(if (pd.prefs.ohioAccent) "command.ohio.on" else "command.ohio.off", p)
        }

        menu.show(player)
    }

    handler.registerCommand("runwave", "<count>", Permission.Admin, CommandRunner { arg: Array<String>, p: Player ->
        if (!Strings.canParseInt(arg[0])) {
            Bundle.sendMessage("command.argument.must-be-number", p, "<count>")
            return@CommandRunner
        }
        val count = Strings.parseInt(arg[0])
        if (count > 10) {
            Bundle.sendMessage("command.argument.must-be-less-than", p, "<count>")
            return@CommandRunner
        }
        for (i in 1..count) {
            Timer.schedule({
                Vars.logic.runWave()
            }, 0.1f + (i / 10f))
        }
    })

    handler.registerCommand("savereplay", "", Permission.Test) { arg: Array<String>, p: Player ->
        /*val file = Vars.dataDirectory.child("replays").child("${arg[0]}.replay")

        file.parent().mkdirs()
        file.writeBytes(saveReplay(History.history, Vars.state.map.name()))

        p.sendMessage("Done!")*/
        if(!S3Enabled) {
            p.sendMessage("S3 not enabled!");
            return@registerCommand
        }
        val mapName = Vars.state.map.name()
        val date = ZonedDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) // yyyy-MM-dd-HH-mm
        val name = "$mapName-${date}.replay"

        PVars.S3.putObject("replays", name, saveReplay(History.copy(), mapName))
        p.sendMessage("[green]Done! Saved with name $name")
    }

    /*handler.registerCommand("playreplay", "<name>", Permission.Test, CommandRunner { arg: Array<String>, p: Player ->
        val file = Vars.dataDirectory.child("replays").child("${arg[0]}.replay")
        if (!file.exists()) {
            p.sendMessage("File doesn't exist!")
            return@CommandRunner
        }
        @OptIn(ExperimentalSerializationApi::class) val replay = ProtoBuf.decodeFromByteArray<Replay>(
            file.readBytes()
        )
        p.sendMessage("total actions: ${replay.actions.size}")
        playReplay(replay.actions)
    })*/

    /*handler.registerCommand("name", "[name...]", CommandRunner { arg: Array<String>, p: Player ->
        if (arg.isEmpty()) {
            getPlayerData(p)?.let { pd: PlayerData ->
                pd.prefs.customName = ""
                pd.updatePrefs()
                Core.app.post { p.name(pd.lastName) }
            }
            return@CommandRunner
        }
        val name = arg[0].trim()
        if (name.length > 100 || Strings.stripColors(name).length > 40) {
            p.sendMessage("[scarlet]Too long!")
            return@CommandRunner
        }
        globalScope.launch {
            getPlayerData(p)?.let { pd: PlayerData ->
                pd.prefs.customName = name
                pd.updatePrefs()
                Core.app.post { p.name(name) }
            }
        }
    })*/

    handler.registerCommand("hub") { _: Array<String>, p: Player ->
        Call.connect(p.con, hubIp, hubPort)
    }

    handler.registerCommand("testmenus", "", Permission.Test) { _: Array<String>, p: Player ->
        val menu = ScrollableMenu(p.coloredName(), "Hi!")
        for (i in 0..15) {
            menu.add("Button $i") { pl2: Player ->
                pl2.sendMessage("You choose $i")
            }
        }
        menu.show(p)
    }

    handler.registerCommand("testtextmenus", "", Permission.Test) { _: Array<String>, p: Player ->
        val menu = ScrollableTextMenu(p.coloredName())
        for (i in 0..15) {
            menu.add("[gold][[[white]$i[gold]][stat] - meow")
        }
        menu.show(p)
    }

    handler.registerCommand("maps", "") { _: Array<String>, p: Player ->
        val menu = ScrollableMenu("@menu.maps.title", rowPerItems = 2)
        val maps = Vars.maps.customMaps()
        for (map in maps) {
            menu.add(Bundle.get("menu.maps.item", p.locale, map.name(), map.height, map.width))
        }
        menu.show(p)
    }

    handler.registerCommand("vanish", "", Permission.Vanish, CommandRunner { _: Array<String>, p: Player ->
        val status = p.getStatus()
        if (status.vanished) {
            status.vanished = false
            Bundle.sendMessage("command.vanish.off", p)
            return@CommandRunner
        }
        Bundle.sendMessage("command.vanish.on", p)
        status.vanished = true
        Call.playerDisconnect(p.id)
    })
    handler.registerCommand("pay", "<amount> <playername...>", CommandRunner { args: Array<String>, player: Player ->
        val target = Groups.player.find { p: Player -> p.plainName().equals(args[1], ignoreCase = true) }
        if (target == null || target === player) {
            Bundle.sendMessage("error.player-not-found", player)
            return@CommandRunner
        }
        if (!Strings.canParseInt(args[0])) {
            Bundle.sendMessage("command.argument.must-be-number", player, "<amount>")
            return@CommandRunner
        }
        val targetStatsOpt = getPlayerData(target)
        val playerStatsOpt = getPlayerData(player)
        if (targetStatsOpt == null || playerStatsOpt == null) {
            Bundle.sendMessage("error.unknown", player)
            return@CommandRunner
        }
        val amount = Strings.parseInt(args[0])
        if (amount < 1) {
            Bundle.sendMessage("command.argument.amount-positive", player)
            return@CommandRunner
        }
        if (amount > playerStatsOpt.balance) {
            Bundle.sendMessage("menu.shop.no-money", player)
            return@CommandRunner
        }
        val commision = (amount * 0.02f).roundToInt()
        playerStatsOpt.subBalance(amount)
        targetStatsOpt.adjBalance(amount - commision)
        target.sendMessage(Bundle.get("command.pay.received", target.locale, player.coloredName(), amount, commision))
        player.sendMessage(Bundle.get("command.pay.sent", player.locale, target.coloredName(), amount, commision))
    })
    handler.registerCommand("economy", "") { _: Array<String?>?, p: Player? ->
        Bundle.infoMessage("command.economy.guide", p)
    }
    /*handler.registerCommand("slot", "<bet>", CommandRunner { a: Array<String>, p: Player ->
        if (!Strings.canParseInt(a[0])) {
            Bundle.sendMessage("command.argument.must-be-number", p, "<bet>")
            return@CommandRunner
        }
        globalScope.launch {
            getPlayerData(p)
                ?.let { s -> Core.app.post { slot(p, s, Strings.parseInt(a[0])) } }
        }
    })*/
    handler.registerCommand("shop", CommandRunner { _: Array<String>, p: Player ->
        if (PVars.gamemode == Gamemode.hexed || PVars.gamemode == Gamemode.crawlerArena) {
            return@CommandRunner
        }
        globalScope.launch {
            getPlayerData(p)?.let { s -> Core.app.post { showShop(s, p) } }
        }
    })
    handler.registerCommand("sync", CommandRunner { _: Array<String>, player: Player ->
        if (Time.timeSinceMillis(player.info.lastSyncTime) < 1000 * 5) {
            Bundle.sendMessage("command.sync.cooldown", player)
            return@CommandRunner
        }
        player.info.lastSyncTime = Time.millis()
        Call.worldDataBegin(player.con)
        Vars.netServer.sendWorldData(player)
    })
    handler.registerCommand("thelp", "[page]", CommandRunner { args: Array<String>, player: Player ->
        if (args.isNotEmpty() && !Strings.canParseInt(args[0])) {
            Bundle.sendMessage("command.argument.must-be-number", player, "<page>")
            return@CommandRunner
        }
        val page = if (args.isNotEmpty()) Strings.parseInt(args[0]) - 1 else 0

        var availableCommands = 0

        val result = StringBuilder()
        val perms = Permission.getPerms(player)

        val pages = Seq<String?>()

        for (i in 0..<handler.commands.size) {
            val c = handler.commands.get(i) ?: continue

            if (!perms.contains(c.permission)) continue

            if (availableCommands >= commandsPerPage) {
                pages.add(result.toString())
                result.setLength(0)
                availableCommands = 0
            }

            availableCommands++
            result.append("[orange]/")
                .append(c.name)
                .append(" [white]")
                .append(c.args)
                .append(" - ")
                .append(c.getDesc(player))
                .append("\n")
        }

        if (result.isNotEmpty()) pages.add(result.toString())

        if ((page + 1) > pages.size || page < 0) {
            Bundle.sendMessage("command.unknown-page", player)
            return@CommandRunner
        }
        val resp = Bundle.get("command.pages.header", player.locale, page + 1, pages.size) + "\n\n" + pages.get(page)
        player.sendMessage(resp)
    })

    handler.registerCommand("help", "") { _: Array<String>, player: Player ->
        val perms = Permission.getPerms(player)
        val menu = ScrollableTextMenu("@menu.help.title")
        for (i in 0..<handler.commands.size) {
            val c = handler.commands.get(i) ?: continue
            if (!perms.contains(c.permission)) continue
            menu.add(
                "[orange]/${c.name}[white]" +
                        (if (c.args.isEmpty()) "" else "\n${c.args}")
                        + " - [lightgray]${c.getDesc(player)}"
            )
        }
        menu.show(player)
    }
    handler.registerCommand("test", "", Permission.Test) { _: Array<String>, p: Player ->
        p.sendMessage("[green]Ok!")
    }

    handler.registerCommand("test2", "", Permission.Test) { _: Array<String>, p: Player ->
        val pd = p.getData() ?: return@registerCommand
        PVars.notificationsChannel.sendMessage("${pd.discordId?.discordMention()} nya")
            .addComponents(ActionRow.of(Button.success(testId, "🦈")))
            .queue()
    }

    handler.registerCommand("stats", "") { _: Array<String>, p: Player ->
        val sb = StringBuilder(Bundle.get("command.stats.header", p.locale)).append("\n")
        getPlayerData(p)?.let { s ->
            s.updateStats(p, false)
            sb.append(Bundle.get("command.stats.blocks-built", p.locale, s.blocksBuild)).append("\n")
            sb.append(Bundle.get("command.stats.blocks-broken", p.locale, s.blocksBroken)).append("\n")
            sb.append(Bundle.get("command.stats.waves-survived", p.locale, s.wavesSurvived)).append("\n")
            sb.append(Bundle.get("command.stats.balance", p.locale, s.balance)).append("\n")
            sb.append(Bundle.get("command.stats.playtime", p.locale, formatTime(s.playtime)))
        }
        p.sendMessage(sb.toString())
    }

    handler.registerCommand("vnw", "[y/n]", CommandRunner { a: Array<String>, p: Player ->
        if (PVars.gamemode == Gamemode.hexed) {
            Bundle.sendMessage("command.disabled-in-hexed", p)
            return@CommandRunner
        }
        val i = if (a.isEmpty()) 1
        else parseBool(a[0])

        if (i == 0) {
            Bundle.sendMessage("command.vote.unknown-choice", p)
            return@CommandRunner
        }
        if (PVars.waveVote == null) {
            PVars.waveVote = VoteWave()
            PVars.waveVote.vote(p, i)
            return@CommandRunner
        }
        if (PVars.waveVote.voted.containsKey(p.ip())) {
            Bundle.sendMessage("command.rtv.already-voted", p)
            return@CommandRunner
        }
        PVars.waveVote.vote(p, i)
    })

    handler.registerCommand("rtv", "[y/n]", CommandRunner { a: Array<String>, p: Player ->
        if (PVars.gamemode == Gamemode.hexed) {
            Bundle.sendMessage("command.disabled-in-hexed", p)
            return@CommandRunner
        }
        val i: Int = if (a.isEmpty()) 1
        else parseBool(a[0])

        if (PVars.mapVote == null) {
            val menu = ScrollableMenu("Choose map", rowPerItems = 2).add("[orange]Random") { pl: Player ->
                if (PVars.mapVote == null) {
                    PVars.mapVote = VoteMap(pl, null)
                    PVars.mapVote.vote(pl, i)
                    return@add
                }
            }
            val maps = Vars.maps.customMaps()
            for (map in maps) {
                menu.add("${map.name()}\n[lightgray]${map.height}x${map.width}") { pl: Player ->
                    if (PVars.mapVote == null) {
                        PVars.mapVote = VoteMap(pl, map)
                        PVars.mapVote.vote(pl, i)
                        return@add
                    }
                }
            }
            menu.show(p)
            return@CommandRunner
        }

        if (i == 0) {
            Bundle.sendMessage("command.vote.unknown-choice", p)
            return@CommandRunner
        }
        if (PVars.mapVote.voted.containsKey(p.ip())) {
            Bundle.sendMessage("command.rtv.already-voted", p)
            return@CommandRunner
        }
        PVars.mapVote.vote(p, i)
    })

    handler.registerCommand(
        "ban",
        "<id> <time> <reason...>",
        Permission.Punish,
        CommandRunner { a: Array<String>, p: Player ->
            if (Strings.canParseInt(a[0])) {
                val id = Strings.parseInt(a[0])
                val time = parseTime(a[1])
                val perm = a[1].equals("perm", ignoreCase = true)
                if (time == -1L && !perm) {
                    Bundle.sendMessage("command.ban.unknown-time", p)
                    return@CommandRunner
                }
                val banned: Boolean = if (perm) {
                    ban(id, p, a[2], -1, "command")
                } else {
                    ban(id, p, a[2], time, "command")
                }
                if (banned) {
                    Bundle.sendMessage("command.ban.success", p)
                } else {
                    Bundle.sendMessage("command.ban.fail", p)
                }
            } else {
                Bundle.sendMessage("command.argument.must-be-number", p, "<id>")
            }
        })

    handler.registerCommand("history") { _: Array<String>, p: Player ->
        val status = p.getStatus()
        if (status.historyEnabled) {
            status.historyEnabled = false
            Bundle.sendMessage("command.history.disabled", p)
            Call.hideHudText(p.con)
        } else {
            status.historyEnabled = true
            Bundle.sendMessage("command.history.enabled", p)
        }
    }

    handler.registerCommand("discord") { _: Array<String>, p: Player ->
        Call.openURI(p.con, PVars.discordLink)
    }

    handler.registerCommand("link", CommandRunner { _: Array<String>, player: Player ->
        val pd = getPlayerData(player) ?: return@CommandRunner
        if (pd.discordId != null) {
            Bundle.sendMessage("command.link.already-linked", player)
            return@CommandRunner
        }

        Menu("@discord.link.select-method", "")
            .add("@discord.link.uri") {
                val req = getLinkRequest(pd.id)
                val state = req?.state ?: run {
                    val s = getRandomString(32)
                    if (!newLinkRequest(s, pd.id)) {
                        Bundle.sendMessage("command.link.error", player)
                        return@add
                    }
                    debug("Generated state(code) for linking $s")
                    s
                }

                Call.openURI(player.con, "$discordOauthBaseUrl/link?state=$state")
            }
            .add("@discord.link.text") {
                var code: String? = player.getLinkCode()
                if (code == null) {
                    code = getRandomString(6)
                    player.setLinkCode(code)
                }

                Bundle.infoMessage("discord.link.instructions", player, PVars.gamemode.botPrefix, code, PVars.discordLink)
            }
            .show(player)
    })

    handler.registerCommand("hidden", "<bool>", Permission.Admin, CommandRunner { a: Array<String>, p: Player ->
        val i = parseBool(a[0])
        val idOpt = getPlayerId(p) ?: return@CommandRunner
        val id: Int = idOpt
        when (i) {
            1 -> {
                updateAdminHidden(id, true)
                p.admin(false)
                Bundle.sendMessage("command.hidden.ok", p)
            }

            -1 -> {
                updateAdminHidden(id, false)
                p.admin(true)
                Bundle.sendMessage("command.hidden.ok", p)
            }

            else -> {
                Bundle.sendMessage("command.hidden.unknown-boolean", p)
            }
        }
    })


    /*
            handler.registerCommand("team", "<team>", (arg, player)->{
                if(!Strings.canParseInt(arg[0])) {
                    sendMessage("command.argument.must-be-number", player, "<team>");
                    return@CommandRunner;
                }
                int id = Strings.parseInt(arg[0]);
                if(id > 5) {
                    sendMessage("command.argument.must-be-less-than", player, "<team>", 5);
                    return@CommandRunner;
                }
                player.team(Team.get(id));
            });*/
    handler.registerCommand("artv", "", Permission.Artv, CommandRunner { _: Array<String>, p: Player ->
        if (PVars.gamemode == Gamemode.hexed) {
            Bundle.sendMessage("command.disabled-in-hexed", p)
            return@CommandRunner
        }
        Events.fire(EventType.GameOverEvent(Team.derelict))
    })

    handler.registerCommand("a", "<message...>", Permission.Admin) { arg: Array<String>, p: Player ->
        val raw = "[#" + Pal.adminChat.toString() + "]<ADM> " + Vars.netServer.chatFormatter.format(p, arg[0])
        globalScope.launch {
            Groups.player.each(
                { pl: Player -> pl.admin || Permission.getPerms(pl).contains(Permission.Admin) },
                { a: Player ->
                    Core.app.post {
                        a.sendMessage(raw, p, arg[0])
                    }
                })
        }
    }

    handler.registerCommand("vote", "<y/n/c>", CommandRunner { arg: Array<String>, player: Player ->
        if (PVars.currentlyKicking == null) {
            // player.sendMessage("[scarlet]Nobody is being voted on.");
            Bundle.sendMessage("command.vote.no-vote-in-progress", player)
        } else {
            if (Permission.getPerms(player).contains(Permission.Admin) && arg[0].equals("c", ignoreCase = true)) {
                // Call.sendMessage(Strings.format("[lightgray]Vote canceled by admin[orange] @[lightgray].", player.name));
                Bundle.sendMessage("command.vote.canceled-by-admin", player.coloredName())
                /*PVars.currentlyKicking.task.cancel()
                PVars.currentlyKicking = null*/
                PVars.currentlyKicking.cancel()
                return@CommandRunner
            }

            if (player.isLocal) {
                // player.sendMessage("[scarlet]Local players can't vote. Kick the player yourself instead.");
                Bundle.sendMessage("command.vote.local-player", player)
                return@CommandRunner
            }

            val sign = when (arg[0].lowercase(Locale.getDefault())) {
                "y", "yes" -> 1
                "n", "no" -> -1
                else -> 0
            }

            //hosts can vote all they want
            if ((PVars.currentlyKicking.voted.get(
                    player.uuid(),
                    2
                ) == sign || PVars.currentlyKicking.voted.get(
                    Vars.netServer.admins.getInfo(player.uuid()).lastIP,
                    2
                ) == sign)
            ) {
                // player.sendMessage(Strings.format("[scarlet]You've already voted @. Sit down.", arg[0].toLowerCase()));
                Bundle.sendMessage("command.vote.already-voted", player, arg[0].lowercase(Locale.getDefault()))
                return@CommandRunner
            }

            if (PVars.currentlyKicking.target === player) {
                // player.sendMessage("[scarlet]You can't vote on your own trial.");
                Bundle.sendMessage("command.vote.cannot-vote-self", player)
                return@CommandRunner
            }

            if (PVars.currentlyKicking.target.team() !== player.team()) {
                // player.sendMessage("[scarlet]You can't vote for other teams.");
                Bundle.sendMessage("command.vote.other-team", player)
                return@CommandRunner
            }

            if (sign == 0) {
                // player.sendMessage("[scarlet]Vote either 'y' (yes) or 'n' (no).");
                Bundle.sendMessage("command.vote.unknown-choice", player)
                return@CommandRunner
            }

            PVars.currentlyKicking.vote(player, sign)
        }
    })

    val cooldowns = ObjectMap<String?, Timekeeper>()
    handler.registerCommand(
        "votekick",
        "[player] [reason...]",
        CommandRunner { args: Array<String>, player: Player ->
            if (!Administration.Config.enableVotekick.bool()) {
                // player.sendMessage("[scarlet]Vote-kick is disabled on this server.");
                Bundle.sendMessage("command.votekick.disabled", player)
                return@CommandRunner
            }
            if (Groups.player.size() < 3) {
                // player.sendMessage("[scarlet]At least 3 players are needed to start a votekick.");
                Bundle.sendMessage("command.votekick.few-players", player)
                return@CommandRunner
            }

            if (player.isLocal) {
                // player.sendMessage("[scarlet]Just kick them yourself if you're the host.");
                Bundle.sendMessage("command.votekick.local-player", player)
                return@CommandRunner
            }

            if (PVars.currentlyKicking != null) {
                // player.sendMessage("[scarlet]A vote is already in progress.");
                Bundle.sendMessage("command.votekick.already-started", player)
                return@CommandRunner
            }
            if (args.isEmpty()) {
                val builder = StringBuilder()
                builder.append(Bundle.get("command.votekick.players", player.locale)).append("\n")

                Groups.player.each(
                    { p: Player -> !p.admin && p.con != null && p !== player },
                    { p: Player ->
                        builder.append("[lightgray] ").append(p.name).append("[accent] (#").append(p.id())
                            .append(")\n")
                    })
                player.sendMessage(builder.toString())
            } else if (args.size == 1) {
                // player.sendMessage("[orange]You need a valid reason to kick the player. Add a reason after the player name.");
                Bundle.sendMessage("command.votekick.no-reason", player)
            } else {
                val found: Player?
                if (args[0].length > 1 && args[0].startsWith("#") && Strings.canParseInt(args[0].substring(1))) {
                    val id = Strings.parseInt(args[0].substring(1))
                    found = Groups.player.find { p: Player -> p.id() == id }
                } else {
                    found = Groups.player.find { p: Player -> p.name.equals(args[0], ignoreCase = true) }
                }

                if (found != null) {
                    val perms = Permission.getPerms(found)
                    if (found === player) {
                        // player.sendMessage("[scarlet]You can't vote to kick yourself.");
                        Bundle.sendMessage("command.votekick.cannot-kick-self", player)
                    } else if (found.admin || perms.contains(Permission.Admin)) {
                        // player.sendMessage("[scarlet]Did you really expect to be able to kick an admin?");
                        Bundle.sendMessage("command.votekick.admin-immune", player)
                    } else if (perms.contains(Permission.VotekickImmune)) {
                        Bundle.sendMessage("command.votekick.immune", player)
                    } else if (found.isLocal) {
                        // player.sendMessage("[scarlet]Local players cannot be kicked.");
                        Bundle.sendMessage("command.votekick.local-player-immune", player)
                    } else if (found.team() !== player.team()) {
                        // player.sendMessage("[scarlet]Only players on your team can be kicked.");
                        Bundle.sendMessage("command.votekick.same-team-only", player)
                    } else {
                        val vtime =
                            cooldowns.get(player.uuid()) { Timekeeper.ofSeconds(voteCooldown.toFloat()) }

                        if (!vtime.get()) {
                            // player.sendMessage("[scarlet]You must wait " + voteCooldown/60 + " minutes between votekicks.");
                            Bundle.sendMessage("command.votekick.cooldown", player, voteCooldown / 60)
                            return@CommandRunner
                        }

                        val session = VotekickSession(found, player, args[1])
                        session.vote(player, 1)
                        // Call.sendMessage(Strings.format("[lightgray]Reason:[orange] @[lightgray].", args[1]));
                        Bundle.sendMessage("command.votekick.start", args[1])
                        vtime.reset()
                        PVars.currentlyKicking = session
                    }
                } else {
                    // player.sendMessage("[scarlet]No player [orange]'" + args[0] + "'[scarlet] found.");
                    Bundle.sendMessage("command.votekick.player-not-found", player, args[0])
                }
            }
        })
}
/*
private fun registerHexedCommands(handler: CustomHandler) {
    handler.registerCommand(
        "spectate",
        ""
    ) { _: Array<String>, player: Player ->
        if (player.team() === Team.derelict) {
            player.sendMessage("[scarlet]You're already spectating.")
        } else {
            hexedGamemode.killTiles(player.team())
            player.unit().kill()
            player.team(Team.derelict)
        }
    }

    handler.registerCommand(
        "captured",
        ""
    ) { _: Array<String>, player: Player ->
        if (player.team() === Team.derelict) {
            player.sendMessage("[scarlet]You're spectating.")
        } else {
            player.sendMessage("[lightgray]You've captured[accent] " + hexedGamemode.data.getControlled(player).size + "[] hexes.")
        }
    }

    handler.registerCommand(
        "leaderboard",
        ""
    ) { _: Array<String>, player: Player ->
        player.sendMessage(hexedGamemode.getLeaderboard())
    }

    handler.registerCommand(
        "hexstatus",
        ""
    ) { _: Array<String>, player: Player ->
        val hex: Hex? = hexedGamemode.data.data(player).location
        if (hex != null) {
            hex.updateController()
            val builder = java.lang.StringBuilder()
            builder.append("| [lightgray]Hex #").append(hex.id).append("[]\n")
            builder.append("| [lightgray]Owner:[] ")
                .append(
                    if (hex.controller != null && hexedGamemode.data.getPlayer(hex.controller) != null) hexedGamemode.data.getPlayer(
                        hex.controller
                    ).name else "<none>"
                )
                .append("\n")
            for (data in Vars.state.teams.getActive()) {
                if (hex.getProgressPercent(data.team) > 0) {
                    builder.append("|> [accent]").append(hexedGamemode.data.getPlayer(data.team).name)
                        .append("[lightgray]: ").append(hex.getProgressPercent(data.team).toInt())
                        .append("% captured\n")
                }
            }
            player.sendMessage(builder.toString())
        } else {
            player.sendMessage("[scarlet]No hex found.")
        }
    }
}

private fun registerCrawlerArenaCommands(handler: CustomHandler) {
    handler.registerCommand(
        "unit",
        "<type> [amount]",
        CommandRunner { args: Array<String>, player: Player ->
            val newUnitType = CrawlerArenaGamemode.findType(args[0].lowercase(Locale.getDefault()))
            if (newUnitType == null) {
                Bundle.sendMessage("command.unit.not-found", player)
                return@CommandRunner
            }
            var amount = 1
            if (args.size == 2) {
                try {
                    amount = args[1].toInt()
                } catch (_: NumberFormatException) {
                    Bundle.sendMessage("command.invalid-amount", player)
                    return@CommandRunner
                }
            }
            if (amount < 1) {
                Bundle.sendMessage("command.invalid-amount", player)
                return@CommandRunner
            }

            if (Groups.unit.count(Boolf { u: Unit -> u.type === newUnitType && u.team === Vars.state.rules.defaultTeam }) > CVars.unitCap - amount) {
                Bundle.sendMessage("command.upgrade.too-many-units", player)
                return@CommandRunner
            }
            if (CrawlerArenaGamemode.money.get(player.uuid(), 0f) >= CVars.unitCosts.get(newUnitType) * amount) {
                if (!player.dead() && player.unit().type === newUnitType || args.size == 2) {
                    for (i in 0..<amount) {
                        val newUnit = newUnitType.spawn(player.x + Mathf.random(), player.y + Mathf.random())
                        CrawlerArenaGamemode.setUnit(newUnit)
                    }
                    CrawlerArenaGamemode.money.put(
                        player.uuid(),
                        CrawlerArenaGamemode.money.get(player.uuid(), 0f) - CVars.unitCosts.get(newUnitType) * amount
                    )
                    Bundle.sendMessage("command.upgrade.extra-unit", player)
                    return@CommandRunner
                }
                val newUnit = newUnitType.spawn(player.x, player.y)
                CrawlerArenaGamemode.setUnit(newUnit, true)
                player.unit(newUnit)
                CrawlerArenaGamemode.money.put(
                    player.uuid(),
                    CrawlerArenaGamemode.money.get(player.uuid(), 0f) - CVars.unitCosts.get(newUnitType)
                )
                CrawlerArenaGamemode.units.put(player.uuid(), newUnitType)
                CrawlerArenaGamemode.unitIDs.put(player.uuid(), newUnit.id)
                Bundle.sendMessage("command.upgrade.success", player)
            } else Bundle.sendMessage("command.upgrade.not-enough-money", player)
        })

    handler.registerCommand(
        "give",
        "<amount> <name...>",
        CommandRunner { args: Array<String>, player: Player ->
            val giveTo = Groups.player.find { p: Player ->
                Strings.stripColors(p.name).lowercase(Locale.getDefault())
                    .contains(args[1].lowercase(Locale.getDefault()))
            }
            if (giveTo == null) {
                Bundle.sendMessage("command.give.player-not-found", player)
                return@CommandRunner
            }
            val amount: Float = if (args[0].equals("all", ignoreCase = true)) {
                CrawlerArenaGamemode.money.get(player.uuid(), 0f)
            } else {
                try {
                    args[0].toInt().toFloat()
                } catch (_: java.lang.NumberFormatException) {
                    Bundle.sendMessage("command.invalid-amount", player)
                    return@CommandRunner
                }
            }
            if (amount < 0) {
                Bundle.sendMessage("command.invalid-amount", player)
                return@CommandRunner
            }
            if (CrawlerArenaGamemode.money.get(player.uuid(), 0f) >= amount) {
                CrawlerArenaGamemode.money.put(
                    player.uuid(),
                    CrawlerArenaGamemode.money.get(player.uuid(), 0f) - amount
                )
                CrawlerArenaGamemode.money.put(
                    giveTo.uuid(),
                    CrawlerArenaGamemode.money.get(giveTo.uuid(), 0f) + amount
                )
                Bundle.sendMessage("command.give.success", player, amount, giveTo.coloredName())
                Bundle.sendMessage("command.give.money-received", giveTo, amount, player.coloredName())
            } else Bundle.sendMessage("command.give.not-enough-money", player)
        })

    handler.registerCommand(
        "info"
    ) { _: Array<String>, player: Player -> Bundle.sendMessage("command.info.message", player) }

    handler.registerCommand(
        "upgrades",
        "[page]",
        CommandRunner { args: Array<String>, player: Player ->
            val page: Int = if (args.isEmpty()) {
                1
            } else {
                try {
                    args[0].toInt()
                } catch (_: java.lang.NumberFormatException) {
                    Bundle.sendMessage("command.invalid-amount", player)
                    return@CommandRunner
                }
            }
            val sortedUnitCosts = CVars.unitCosts.values().toArray()
            val maxPage = (sortedUnitCosts.size - 1) / CVars.unitsRows + 1
            if (page !in 1..maxPage) {
                Bundle.sendMessage("command.invalid-amount", player)
                return@CommandRunner
            }
            sortedUnitCosts.sort()
            if (page < maxPage) {
                sortedUnitCosts.removeRange(CVars.unitsRows * page, sortedUnitCosts.size - 1)
            }
            if (page > 1) {
                sortedUnitCosts.removeRange(0, CVars.unitsRows * (page - 1) - 1)
            }
            val unitCostsCopy = ObjectIntMap<UnitType>()
            unitCostsCopy.putAll(CVars.unitCosts)

            //StringBuilder upgrades = new StringBuilder(Bundle.format("commands.upgrades.header", Bundle.findLocale(player)));
            val upgrades = java.lang.StringBuilder(Bundle.get("command.upgrades.header", player.locale))

            upgrades.append(Bundle.get("command.upgrades.page", player.locale, page, maxPage)).append("\n")
            sortedUnitCosts.each { cost: Int ->
                val type = unitCostsCopy.findKey(cost)
                upgrades.append("[gold] - [accent]").append(type.name).append(" [lightgray](").append(cost)
                    .append(")\n")
                unitCostsCopy.remove(type)
            }
            player.sendMessage(upgrades.toString())
        })

    handler.registerCommand(
        "cost",
        "<type>",
        CommandRunner { args: Array<String>, player: Player ->
            val type = CrawlerArenaGamemode.findType(args[0].lowercase(Locale.getDefault()))
            val cost = CVars.unitCosts.get(type, -1)
            if (cost == -1) {
                Bundle.sendMessage("command.unit.not-found", player)
                return@CommandRunner
            }
            player.sendMessage(type.name + (" - ") + (cost))
        })
}
 */