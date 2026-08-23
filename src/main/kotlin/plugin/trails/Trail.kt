package plugin.trails

import arc.graphics.Color
import mindustry.entities.Effect

data class Trail(
    val name: String,
    val effect: Effect,
    val color: Color,
) {
    fun withColor(color: Color): Trail = copy(color = color)
}
