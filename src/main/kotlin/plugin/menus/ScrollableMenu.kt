package plugin.menus

import arc.struct.Seq
import mindustry.gen.Player
import plugin.Bundle

class ScrollableMenu(
    private val title: String,
    private val message: String = "",
    private val itemsPerPage: Int = 9,
    private val rowPerItems: Int = 3,
) {
    private val items = Seq<String>()
    private val handlers = Seq<(Player) -> Unit>()

    private val footerItems = Seq<String>()
    private val footerHandlers = Seq<(Player) -> Unit>()

    fun add(
        text: String,
        handler: (Player) -> Unit,
    ): ScrollableMenu {
        items.add(text)
        handlers.add(handler)
        return this
    }

    fun add(text: String): ScrollableMenu {
        add(text) {} // empty handler
        return this
    }

    fun addFooter(
        text: String,
        handler: (Player) -> Unit,
    ): ScrollableMenu {
        footerItems.add(text)
        footerHandlers.add(handler)
        return this
    }

    fun show(player: Player) {
        showPage(player, 0)
    }

    private fun showPage(
        player: Player,
        page: Int,
    ) {
        val totalPages =
            kotlin.math
                .ceil(items.size / itemsPerPage.toDouble())
                .toInt()
                .coerceAtLeast(1)

        val menu =
            Menu(
                title,
                if (message.isEmpty()) {
                    Bundle.get("menu.page", player.locale, page + 1, totalPages)
                } else {
                    message + "\n" + Bundle.get("menu.page", player.locale, page + 1, totalPages)
                },
            )

        val start = page * itemsPerPage
        val end = minOf(start + itemsPerPage, items.size)

        for (i in start until end) {
            menu.add(items[i]) { p ->
                handlers[i](p)
            }

            if ((i - start + 1) % rowPerItems == 0) {
                menu.row()
            }
        }

        if (!footerItems.isEmpty) {
            menu.row()
            for (i in 0 until footerItems.size) {
                menu.add(footerItems[i]) { p -> footerHandlers[i](p) }
            }
        }

        menu.row()

        menu.add("@menu.prev") { p ->
            val newPage = (page - 1).coerceAtLeast(0)
            showPage(p, newPage)
        }

        menu.add("@menu.cancel") { p ->
            p.sendMessage(Bundle.get("menu.cancelled", p.locale))
        }

        menu.add("@menu.next") { p ->
            val newPage = (page + 1).coerceAtMost(totalPages - 1)
            showPage(p, newPage)
        }

        menu.show(player)
    }
}
