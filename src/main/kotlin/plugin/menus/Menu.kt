package plugin.menus

import arc.Events
import arc.func.Cons
import arc.struct.ObjectMap
import arc.struct.Seq
import mindustry.game.EventType
import mindustry.gen.Call
import mindustry.gen.Player
import plugin.Bundle

class Menu(var title: String, var message: String) {
    lateinit var player: Player
    var id: Int = 0
    val rows: Seq<Seq<String>> = Seq()
    val handlers: Seq<Cons<Player>> = Seq()

    fun add(text: String, handler: Cons<Player>): Menu {
        if (rows.isEmpty) {
            rows.add(Seq<String>())
        }

        rows.peek().add(text)
        handlers.add(handler)

        return this
    }

    fun add(text: String): Menu = add(text) { }

    fun show(player: Player): Menu {
        this.player = player
        lastId += 1
        this.id = lastId

        menus.put(id, this)

        Call.menu(player.con, id, resolve(title, player), resolve(message, player), buildRows(player))

        return this
    }

    fun resolve(text: String, player: Player): String {
        if (text.startsWith("@")) {
            val key = text.substring(1)
            val value = Bundle.get(key, player.locale)
            if (value != key) return value
        }

        return text
    }

    fun row(): Menu {
        rows.add(Seq<String>())
        return this
    }

    fun buildRows(player: Player): Array<Array<String>> = Array(rows.size) { i ->
        val row = rows[i]
        Array(row.size) { o -> resolve(row[o], player) }
    }

    companion object {
        var lastId = 0
        val menus = ObjectMap<Int, Menu>()

        fun load() {
            Events.on(EventType.PlayerLeave::class.java) { e ->
                val toRemove = Seq<Int>()
                menus.each { id, menu ->
                    if (menu.player == e.player) toRemove.add(id)
                }
                toRemove.each { menus.remove(it) }
            }

            Events.on(EventType.MenuOptionChooseEvent::class.java) { e ->
                val player = e.player
                val menu = menus.get(e.menuId)
                if (menu == null || menu.player != player) {
                    player.sendMessage(Bundle.get("menu.invalid", player.locale))
                    return@on
                }

                val option = e.option
                if (option == -1) {
                    player.sendMessage(Bundle.get("menu.closed", player.locale))
                    menus.remove(e.menuId)
                    return@on
                }
                if (option > menu.handlers.size) {
                    player.sendMessage(Bundle.get("menu.invalidoption", player.locale))
                    return@on
                }
                menus.remove(e.menuId)
                menu.handlers[option].get(player)
            }
        }
    }
}