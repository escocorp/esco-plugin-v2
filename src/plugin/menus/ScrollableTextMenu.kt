package plugin.menus

import arc.struct.Seq
import mindustry.gen.Player

class ScrollableTextMenu(
    private val title: String
) {

    private val pages = Seq<Seq<String>>()
    private var currentPage = Seq<String>()

    fun add(text: String): ScrollableTextMenu {
        currentPage.add(text)
        return this
    }

    fun row(): ScrollableTextMenu {
        pages.add(currentPage)
        currentPage = Seq()
        return this
    }

    fun show(player: Player) {
        if (!currentPage.isEmpty) {
            pages.add(currentPage)
            currentPage = Seq()
        }

        showPage(player, 0)
    }

    private fun showPage(player: Player, page: Int) {
        val totalPages = pages.size.coerceAtLeast(1)

        val pageText = buildString {
            if (page < pages.size) {
                append(pages[page].toString("\n"))
            }
            append("\n\n[gray]Page ${page + 1}/$totalPages")
        }

        val menu = Menu(title, pageText)

        menu.row()

        menu.add("[lightgray]<-") { p ->
            showPage(p, (page - 1).coerceAtLeast(0))
        }

        menu.add("[red]Cancel") { p ->
            p.sendMessage("[red]Cancelled.")
        }

        menu.add("[lightgray]->") { p ->
            showPage(p, (page + 1).coerceAtMost(totalPages - 1))
        }

        menu.show(player)
    }
}