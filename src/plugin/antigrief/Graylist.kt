package plugin.antigrief

import arc.struct.Seq
import arc.util.Http
import arc.util.Log
import arc.util.Reflect
import arc.util.Timer
import mindustry.Vars
import mindustry.gen.Player
import mindustry.net.ArcNetProvider
import plugin.Bundle
import plugin.PVars
import plugin.database.models.PlayerData
import plugin.database.models.putLog
import java.net.InetAddress

const val baseUrl = "https://raw.githubusercontent.com/escocorp/graylist/refs/heads/main"
const val ispsUrl = "$baseUrl/isps.txt"
const val ipsUrl = "$baseUrl/ips.txt"
const val ipsBlockUrl = "$baseUrl/ips-filter.txt"

val isps = Seq<String>()
val ips = Seq<String>()
val ipsBlock = Seq<String>()

fun apply(p: Player, isp: String?, pd: PlayerData) {
    if (pd.discordId == null && (isps.contains(isp) || ipMatches(ips, p.ip()))) {
        p.kick(Bundle.get("kick.graylisted", p.locale, PVars.discordLink), 0)
        putLog(pd.id, "graylist", "Player graylisted by IP ${p.ip()}")
    }
}

fun loadGraylist() {
    Timer.schedule({
        reloadGraylist()
    }, 0f, 30f * 60)

    Reflect.get<ArcNetProvider>(Vars.net, "provider").setConnectFilter { ip ->
        !ipMatches(ipsBlock, ip)
    }
}

fun reloadGraylist() {
    try {
        Http.get(ispsUrl)
            .error { e ->
                Log.err("Failed to load ISPs list", e)
            }
            .submit { response ->
                isps.clear()
                isps.addAll(
                    response.resultAsString
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
                Log.info("Loaded ${isps.size} ISPs")
            }

        Http.get(ipsUrl)
            .error { e ->
                Log.err("Failed to load IPs list", e)
            }
            .submit { response ->
                ips.clear()
                ips.addAll(
                    response.resultAsString
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
                Log.info("Loaded ${ips.size} IPs")
            }

        Http.get(ipsBlockUrl)
            .error { e ->
                Log.err("Failed to load blocked IPs list", e)
            }
            .submit { response ->
                ipsBlock.clear()
                ipsBlock.addAll(
                    response.resultAsString
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
                Log.info("Loaded ${ipsBlock.size} blocked IPs")
            }
    } catch (e: Exception) {
        Log.err("Failed to load graylist", e)
    }
}

private fun ipMatches(list: Seq<String>, ip: String): Boolean {
    for (entry in list) {
        if (entry == ip) return true

        if (entry.contains('/') && cidrMatch(ip, entry)) {
            return true
        }
    }

    return false
}

private fun cidrMatch(ip: String, cidr: String): Boolean {
    return try {
        val parts = cidr.split("/", limit = 2)
        if (parts.size != 2) return false

        val network = InetAddress.getByName(parts[0]).address
        val address = InetAddress.getByName(ip).address
        val prefix = parts[1].toInt()

        if (network.size != address.size) return false

        var bits = prefix

        for (i in network.indices) {
            if (bits <= 0) break

            val mask = if (bits >= 8) {
                0xFF
            } else {
                (0xFF shl (8 - bits)) and 0xFF
            }

            if ((network[i].toInt() and mask) != (address[i].toInt() and mask)) {
                return false
            }

            bits -= 8
        }

        true
    } catch (_: Exception) {
        false
    }
}