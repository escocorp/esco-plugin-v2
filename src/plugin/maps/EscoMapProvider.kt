package plugin.maps

import mindustry.Vars
import mindustry.game.Gamemode
import mindustry.maps.Map
import mindustry.maps.Maps

class EscoMapProvider : Maps.MapProvider {
    override fun next(mode: Gamemode, previous: Map?): Map? {
        val custom = Vars.maps.customMaps().copy()
        if(previous != null)
            custom.remove(previous)
        return custom.shuffle().peek()
    }
}