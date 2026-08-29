// stolen from old escoplugin + fixed by ai
package plugin.menus

import arc.Events
import arc.func.Cons2
import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.Timer
import mindustry.game.EventType
import mindustry.gen.Call
import mindustry.gen.Player
import plugin.Bundle

class TextMenu(
    private val handler: Cons2<Player, String>,
) {
    lateinit var player: Player
    val id: Int = ++lastId

    var title: String = "No title"
    var message: String = "Write here"
    var defMessage: String = ""
    var len: Int = 32
    var numeric: Boolean = false

    private var invalidTimer: Timer.Task? = null

    fun setTitle(value: String): TextMenu {
        title = value
        return this
    }

    fun setMessage(value: String): TextMenu {
        message = value
        return this
    }

    fun setDefMessage(value: String): TextMenu {
        defMessage = value
        return this
    }

    fun setLen(value: Int): TextMenu {
        len = value
        return this
    }

    fun setNumeric(value: Boolean): TextMenu {
        numeric = value
        return this
    }

    fun show(p: Player): TextMenu {
        Call.textInput(p.con, id, title, message, len, defMessage, numeric)

        player = p

        menus.put(id, this)

        invalidTimer = Timer.schedule({ cancel() }, 60f)

        return this
    }

    fun cancel() {
        menus.remove(id)
        invalidTimer?.cancel()
        invalidTimer = null
    }

    companion object {
        var lastId = 0
        val menus = ObjectMap<Int, TextMenu>()

        fun load() {
            Events.on(EventType.PlayerLeave::class.java) { e ->
                val toRemove = Seq<Int>()
                menus.each { id, menu ->
                    if (menu.player == e.player) toRemove.add(id)
                }
                toRemove.each { id ->
                    menus.get(id)?.cancel()
                }
            }

            Events.on(EventType.TextInputEvent::class.java) { e ->
                val player = e.player
                val text = e.text

                val menu = menus.get(e.textInputId)
                if (menu == null || menu.player != player) {
                    player.sendMessage(Bundle.get("menu.invalid-input", player.locale))
                    return@on
                }

                menu.cancel()
                menu.handler.get(player, text)
            }
        }
    }
}
