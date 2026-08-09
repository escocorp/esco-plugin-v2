package plugin.trails

import arc.graphics.Color
import mindustry.content.Fx

enum class Trails(val trail: Trail) {
    Mine(Trail("mine", Fx.mine, Color.sky)),
    HealWave(Trail("healWave", Fx.healWave, Color.lime)),
    Explosion(Trail("explosion", Fx.plasticExplosionFlak, Color.orange)),
    Smoke(Trail("smoke", Fx.smoke, Color.gray)),
    SmokeCloud(Trail("smokeCloud", Fx.smokeCloud, Color.gray));

    companion object {
        fun parse(name: String): Trails? =
            entries.firstOrNull { it.trail.name.equals(name, ignoreCase = true) }
    }
}
