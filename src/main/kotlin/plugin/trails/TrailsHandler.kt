// stolen from old escoplugin
package plugin.trails

import arc.Core
import arc.util.Timer
import mindustry.gen.Call
import mindustry.gen.Player
import plugin.commands.CustomHandler
import plugin.menus.ScrollableMenu
import plugin.utils.sendBundle

object TrailsHandler {
    private const val UPDATE_INTERVAL = 0.1f
    private const val ITEMS_PER_ROW = 2

    private val activeTrails = HashMap<Player, Trail>()

    fun load() {
        Timer.schedule({
            Core.app.post(::render)
        }, 0f, UPDATE_INTERVAL)
    }

    /**
     * Registers the `/trail` command.
     */
    fun registerCommands(handler: CustomHandler) {
        handler.registerCommand("trail", "") { _, player ->
            showMenu(player)
        }
    }

    /**
     * Checks whether a player is allowed to use the given trail.
     * Currently, a stub that grants access to everyone.
     */
    @Suppress("UNUSED_PARAMETER")
    fun hasAccess(
        player: Player,
        trail: Trails,
    ): Boolean = true

    fun remove(player: Player) {
        activeTrails.remove(player)
    }

    private fun showMenu(player: Player) {
        val menu = ScrollableMenu("@menu.trails.title", "@menu.trails.message", rowPerItems = ITEMS_PER_ROW)

        for (trails in Trails.entries) {
            menu.add(itemLabel(player, trails)) { p -> select(p, trails) }
        }

        menu.addFooter("@menu.trails.disable") { p ->
            if (activeTrails.remove(p) != null) p.sendBundle("command.trail.disabled")
        }

        menu.show(player)
    }

    private fun itemLabel(
        player: Player,
        trails: Trails,
    ): String {
        val trail = trails.trail
        val marker = if (activeTrails[player]?.name == trail.name) "[green]> " else ""
        return "$marker[#${trail.color}]${trail.name}"
    }

    private fun select(
        player: Player,
        trails: Trails,
    ) {
        if (!hasAccess(player, trails)) {
            player.sendBundle("command.trail.no-access")
            return
        }

        activeTrails[player] = trails.trail
        player.sendBundle("command.trail.enabled", trails.trail.name)
    }

    private fun render() {
        val iterator = activeTrails.iterator()
        while (iterator.hasNext()) {
            val (player, trail) = iterator.next()

            if (!player.con.isConnected) {
                iterator.remove()
                continue
            }

            val unit = player.unit()
            if (unit == null || unit.dead) continue

            Call.effect(trail.effect, unit.x, unit.y, 1f, trail.color)
        }
    }
}
