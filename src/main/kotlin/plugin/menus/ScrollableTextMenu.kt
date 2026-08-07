package plugin.menus

import arc.struct.Seq
import mindustry.gen.Player

class ScrollableTextMenu(
    val title: String,
    val itemsPerPage: Int = 5
) {

    private val items = Seq<String>()

    fun add(text: String): ScrollableTextMenu {
        items.add(text)
        return this
    }

    fun show(player: Player) {
        showPage(player, 0)
    }

    private fun showPage(player: Player, page: Int) {
        val totalPages = kotlin.math.ceil(items.size / itemsPerPage.toDouble()).toInt().coerceAtLeast(1)

        val start = page * itemsPerPage
        val end = minOf(start + itemsPerPage, items.size)

        val pageText = buildString {
            for (i in start until end) {
                appendLine(items[i])
            }
            append("\n[gray]Page ${page + 1}/$totalPages")
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