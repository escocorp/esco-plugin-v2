package plugin.menus

import arc.struct.Seq
import mindustry.gen.Player
import plugin.Bundle

class PagedMenu(
    private val title: String,
    private val message: String = ""
) {

    private data class Item(
        val text: String,
        val handler: (Player) -> Unit,
        val rowAfter: Boolean
    )

    private val pages = Seq<Seq<Item>>()
    private var currentPage = Seq<Item>()

    init {
        pages.add(currentPage)
    }

    fun add(
        text: String,
        handler: (Player) -> Unit = {}
    ): PagedMenu {
        currentPage.add(Item(text, handler, false))
        return this
    }

    fun row(): PagedMenu {
        if (currentPage.any()) {
            val last = currentPage.peek()
            currentPage.set(
                currentPage.size - 1,
                last.copy(rowAfter = true)
            )
        }
        return this
    }

    fun page(): PagedMenu {
        currentPage = Seq()
        pages.add(currentPage)
        return this
    }

    fun show(player: Player) {
        showPage(player, 0)
    }

    private fun showPage(player: Player, page: Int) {
        val menu = Menu(
            title,
            if (message.isEmpty())
                Bundle.get("menu.page", player.locale, page + 1, pages.size)
            else
                message + "\n" + Bundle.get("menu.page", player.locale, page + 1, pages.size)
        )

        for (item in pages[page]) {
            menu.add(item.text) { p ->
                item.handler(p)
            }

            if (item.rowAfter) {
                menu.row()
            }
        }

        menu.row()

        menu.add("@menu.prev") { p ->
            if (page > 0)
                showPage(p, page - 1)
        }

        menu.add("@menu.cancel") {
            it.sendMessage(Bundle.get("menu.cancelled", it.locale))
        }

        menu.add("@menu.next") { p ->
            if (page < pages.size - 1)
                showPage(p, page + 1)
        }

        menu.show(player)
    }
}