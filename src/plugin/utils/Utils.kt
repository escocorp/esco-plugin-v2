package plugin.utils

import mindustry.gen.Player
import plugin.utils.Permission.*;

fun Player.hasPerms(perm: Permission): Boolean {
    return getPerms(this).contains(perm)
}