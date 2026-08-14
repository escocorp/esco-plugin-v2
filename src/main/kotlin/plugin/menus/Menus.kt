package plugin.menus

import arc.func.Cons
import arc.func.Cons2
import arc.struct.ObjectIntMap
import arc.struct.Seq
import mindustry.Vars
import mindustry.content.Items.*
import mindustry.content.UnitTypes
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.type.Item
import mindustry.type.UnitType
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import plugin.Bundle
import plugin.PVars
import plugin.database.models.Permission
import plugin.database.models.PlayerData
import plugin.database.models.ban
import plugin.database.models.getPlayerData
import plugin.model.getStatus
import plugin.utils.Gamemode
import plugin.utils.parseBool
import plugin.utils.parseTime
import java.text.MessageFormat
import java.util.function.Consumer

val unitCosts = ObjectIntMap<UnitType>()
val itemCosts = ObjectIntMap<Item>()

fun loadMenus() {
    unitCosts.putAll(
        UnitTypes.crawler, 50,
        UnitTypes.dagger, 100,
        UnitTypes.flare, 100,
        UnitTypes.mono, 100,
        UnitTypes.renale, 700,
        UnitTypes.poly, 700,
        UnitTypes.mace, 700,
        UnitTypes.mega, 1550,
        UnitTypes.fortress, 1550
    )
    itemCosts.putAll(
        coal, 1000,
        sand, 1000,
        scrap, 1000,
        copper, 1500,
        lead, 2000,
        pyratite, 2000,
        graphite, 2500,
        silicon, 2500,
        titanium, 2500,
    )
}

fun showShop(stats: PlayerData, p: Player) {
    val menu = ScrollableMenu(
        Bundle.get("menu.shop.title", p.locale),
        Bundle.get("menu.shop.balance", p.locale, stats.balance),
        rowPerItems = 1
    )

    menu.add(Bundle.get("menu.shop.category.units", p.locale)) { pl ->
        val countMenu = Menu("@menu.shop.count.title", "@menu.shop.count.message")
        countMenu.add("1") {
            buyUnits(stats, p, 1)
        }
        countMenu.add("5") {
            buyUnits(stats, p, 5)
        }
        countMenu.add("10") {
            buyUnits(stats, p, 10)
        }
        countMenu.row().add("@menu.close")
        countMenu.show(pl)
    }
    menu.add(Bundle.get("menu.shop.category.items", p.locale)) { pl ->
        val itemMenu =
            ScrollableMenu(Bundle.get("menu.shop.title", p.locale), Bundle.get("menu.shop.balance", p.locale, stats.balance))
        itemCosts.forEach(Consumer { en: ObjectIntMap.Entry<Item> ->
            val type = en.key
            val cost = if (PVars.gamemode == Gamemode.pvp) en.value * 3 else en.value
            itemMenu.add(Bundle.get("menu.shop.cost", p.locale, type.emoji(), cost)) { pl: Player ->
                if (cost > stats.balance) {
                    Bundle.sendMessage("menu.shop.no-money", pl)
                    return@add
                }
                pl.team().core().items.add(type, 1000)
                stats.subBalance(cost)
                Bundle.label("menu.shop.unit-bought", 1f, pl.x, pl.y, pl.coloredName(), type.emoji(), cost)
            }
        })
        itemMenu.show(pl)
    }
    menu.add(Bundle.get("menu.shop.category.other", p.locale)) { pl ->
        val otherMenu =
            ScrollableMenu(Bundle.get("menu.shop.title", p.locale), Bundle.get("menu.shop.balance", p.locale, stats.balance))
        if (PVars.gamemode != Gamemode.pvp) {
            otherMenu.add(
                Bundle.get(
                    "menu.shop.cost",
                    p.locale,
                    Bundle.get("menu.shop.healcores", p.locale),
                    2500
                )
            ) { pl: Player ->
                if (2500 > stats.balance) {
                    Bundle.sendMessage("menu.shop.no-money", pl)
                    return@add
                }
                // sendMessage("menu.shop.cores-repaired", pl.coloredName(), pl.team().emoji);
                Bundle.label("menu.shop.cores-repaired", 1f, pl.x, pl.y, pl.coloredName(), pl.team().emoji)
                pl.team().cores().each(Cons { obj: CoreBuild -> obj.heal() })
            }
        }
        otherMenu.show(pl)
    }

    menu.show(p)
}

fun buyUnits(stats: PlayerData, p: Player, count: Int) {
    val unitMenu =
        ScrollableMenu(Bundle.get("menu.shop.title", p.locale), Bundle.get("menu.shop.balance", p.locale, stats.balance))

    unitCosts.forEach(Consumer { en: ObjectIntMap.Entry<UnitType> ->
        val type = en.key
        val cost = (if (PVars.gamemode == Gamemode.pvp) en.value * 3 else en.value) * count
        unitMenu.add(Bundle.get("menu.shop.cost", p.locale, type.emoji(), cost)) { pl: Player ->
            if (cost > stats.balance) {
                Bundle.sendMessage("menu.shop.no-money", pl)
                return@add
            }

            @Suppress("UNUSED_PARAMETER", "RedundantSuppression")
            for(i in 0 until count)
                type.spawn(pl.team(), pl.x, pl.y)
            stats.subBalance(cost)

            //sendMessage("menu.shop.unit-bought", pl.coloredName(), type.emoji(), cost);
            Bundle.label("menu.shop.unit-bought", 1f, pl.x, pl.y, pl.coloredName(), type.emoji(), cost)
        }
    })

    unitMenu.show(p)
}

fun showWelcome(p: Player) {
    Menu(Bundle.get("menu.welcome.title", p.locale), Bundle.get("menu.welcome.message", p.locale))
        .add(Bundle.get("menu.close"))
        .add(
            "[blue]\uE80DDiscord"
        ) { pl: Player -> Call.openURI(pl.con, PVars.discordLink) }
        .add(Bundle.get("menu.dont-show", p.locale)) { pl: Player ->
            getPlayerData(pl)?.let { data: PlayerData ->
                data.prefs.showWelcomeMenu = false
                data.updatePrefs()
            }
        }
        .show(p)
}

fun showTrace(p: Player, other: Player, perms: Seq<Permission?>) {
    val menu = Menu("@menu.info.title", "")
    val stats = Vars.netServer.admins.getInfo(other.uuid())
    val status = other.getStatus()

    menu.add(Bundle.get("menu.trace.name", p.locale) + "\n" + other.coloredName()) { pl: Player ->
        Call.infoMessage(pl.con, stats.names.toString("\n"))
    }.row()
    val pdOpt = getPlayerData(other)
    pdOpt?.let { d: PlayerData? ->
        menu.add(Bundle.get("menu.trace.id", p.locale) + "\n" + d!!.id).row()
    }
    menu.add(Bundle.get("menu.trace.locale", p.locale) + "\n" + other.locale).row()
        .add(Bundle.get("menu.trace.ip", p.locale) + "\n" + other.ip()) { pl: Player ->
            Call.infoMessage(pl.con, stats.ips.toString("\n"))
        }.row()
        .add(Bundle.get("menu.trace.mobile", p.locale) + "\n" + other.con.mobile).row()
        .add(Bundle.get("menu.trace.client", p.locale) + "\n${other.con.modclient}") {
            Bundle.infoMessage(
                "menu.trace.mod-stats",
                it,
                parseBool(status.schemeSizeUser, colored = true),
                parseBool(status.foosUser, colored = true),
                parseBool(status.agzamModUser, colored = true)
            )
        }.row()
        .add(Bundle.get("menu.trace.joined", p.locale) + "\n" + stats.timesJoined).row()
        .add(Bundle.get("menu.trace.kicked", p.locale) + "\n" + stats.timesKicked).row()
    if (perms.contains(Permission.Punish) && pdOpt != null) {
        menu.add("@menu.trace.ban") { pl2: Player ->
            showBanMenu(pl2, pdOpt.id, other)
        }.row()
    }
    menu.add("@menu.close")
        .show(p)
}

fun showBanMenu(p: Player, playerId: Int, target: Player) {
    TextMenu(Cons2 { pl: Player, reason: String ->
        if (reason.isEmpty()) {
            pl.sendMessage(Bundle.get("menu.ban.no-reason", pl.locale))
            return@Cons2
        }
        TextMenu(Cons2 { pl2: Player, time: String ->
            if (time.isEmpty()) {
                pl2.sendMessage(Bundle.get("menu.ban.no-time", pl2.locale))
                return@Cons2
            }
            val timeL = parseTime(time)
            if (timeL == -1L && !time.contains("perm")) {
                pl2.sendMessage(Bundle.get("menu.ban.unknown-time", pl2.locale))
                return@Cons2
            }
            if (ban(playerId, pl2, reason, timeL, "menu")) {
                pl2.sendMessage(Bundle.get("menu.ban.success", pl2.locale))
                target.kick(
                    MessageFormat.format(
                        Bundle.get("kick.banned"),
                        reason,
                        time,
                        PVars.discordLink,
                        "unknown (re-join to see)"
                    ), 0
                )
            } else {
                pl2.sendMessage(Bundle.get("menu.ban.fail", pl2.locale))
            }
        }).setTitle(Bundle.get("menu.ban.time.title", p.locale)).setMessage(Bundle.get("menu.ban.time.message", p.locale)).show(pl)
    }).setTitle(Bundle.get("menu.ban.reason.title", p.locale)).setMessage(Bundle.get("menu.ban.reason.message", p.locale)).show(p)
}