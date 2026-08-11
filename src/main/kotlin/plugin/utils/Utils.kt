package plugin.utils

import arc.Core
import arc.Events
import arc.files.Fi
import arc.func.Cons
import arc.math.geom.Point2
import arc.net.Connection
import arc.util.Http
import arc.util.Log
import arc.util.Reflect
import arc.util.Strings
import kotlinx.coroutines.launch
import mindustry.Vars
import mindustry.Vars.saveDirectory
import mindustry.Vars.saveExtension
import mindustry.ai.UnitCommand
import mindustry.core.GameState
import mindustry.ctype.UnlockableContent
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.io.SaveIO
import mindustry.maps.Map
import mindustry.net.NetConnection
import mindustry.world.Block
import mindustry.world.blocks.power.PowerNode
import mindustry.world.blocks.units.UnitFactory
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import plugin.Bundle
import plugin.KVars.eventsScope
import plugin.PVars
import plugin.PVars.apiAuth
import plugin.PVars.httpClient
import plugin.database.models.Permission
import plugin.database.models.Permission.Companion.getPerms
import plugin.model.VPNApiResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.*
import java.util.zip.InflaterInputStream
import javax.imageio.ImageIO
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val characters = "qwertyuiopasdfghjklzxcvbnm123456789"

/**
 * Checks if an IP is a proxy/VPN/anonymizer
 * [ip] - Target IP to check
 * */
fun isAnon(ip: String?, callback: Cons<VPNApiResponse>) {
    Http.get(PVars.vpnApi + ip)
        .header("Authorization", "Basic $apiAuth")
        .error { th ->
            Log.err("Failed to check ip $ip", th)
        }
        .submit { resp ->
            Log.debug("Received IpApi response")
            try {
                val apiResponse = PVars.objectMapper.readValue(
                    resp.resultAsString,
                    VPNApiResponse::class.java
                )
                if (!apiResponse.status.equals("success")) {
                    Log.err("Failed to check ip $ip messsage ${apiResponse.message}")
                    return@submit
                }
                callback.get(
                    apiResponse
                )
            } catch (e: Exception) {
                Log.err("Failed to parse api response", e)
            }
        }
    /*
    Http.get(
        PVars.vpnApi + ip,
        { resp: Http.HttpResponse? ->
            try {
                callback.get(
                    PVars.objectMapper.readValue(
                        resp!!.resultAsString,
                        ApiResponse::class.java
                    )
                )
            } catch (e: Exception) {
                Log.err("Failed to parse api response", e)
            }
        },
        { err: Throwable? ->
            Log.err("Failed to check ip", err)
        }
    )*/
}

/**
 * Download file to some path
 * [url] - target url
 * */
fun download(url: String, dest: Path) {
    val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
    val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofFile(dest))
    require(resp.statusCode() in 200..299) { "HTTP ${resp.statusCode()}" }
}

/**
 * sends GET request
 * [url] - target url
 * */
fun httpGetString(url: String): String {
    val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
    val resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString())
    require(resp.statusCode() in 200..299) { "HTTP ${resp.statusCode()}" }
    return resp.body().trim()
}
/**
 * converts y/yes/true to bool
 * [bool] - bool as string
 * */
fun parseBool(bool: String): Int {
    return when (bool.lowercase(Locale.getDefault())) {
        "y", "yes", "д", "да", "+", "t", "true" -> 1
        "n", "no", "н", "нет", "-", "f", "false" -> -1
        else -> 0
    }
}

/**
 * converts bool to string
 * [bool] - target bool
 * [colored] Should the output be colored
 * */
fun parseBool(bool: Boolean, colored: Boolean = false): String =
    when {
        colored && bool -> "[green]Yes"
        colored -> "[red]No"
        bool -> "Yes"
        else -> "No"
    }

/**
 * generates random string without spec. symbols
 * [len] - output string length
 * */
fun getRandomString(len: Int): String {
    val sb = StringBuilder()

    for (i in 0..<len) {
        sb.append(characters[PVars.random.nextInt(characters.length)])
    }

    return sb.toString()
}

/**
 * Resolves a resource file from the plugin's root directory.
 *
 * @param name relative path to the resource inside the plugin root
 * @return the resource as [Fi], or `null` if the plugin is not found
 *         or the resource does not exist
 */
fun getResource(name: String): Fi? {
    return Vars.mods.locateMod("plugin").root.child(name)
}

/**
 * Removes all characters in the [0xF80, 0x107F] range (Tibetan and Myanmar blocks)
 * from the given string.
 *
 * @param string the string to filter
 * @return the string without the stripped characters
 */
fun stripFoo(string: String): String {
    val var1 = StringBuilder(string)
    for (i in string.length - 1 downTo 0) {
        if (var1[i].code in 0xf80..0x107f) var1.deleteCharAt(i)
    }
    return var1.toString()
}
/**
 * Formats a duration in seconds into a compact string like `1d2h3m4s`.
 * Units with a zero value are omitted; if the result would be empty, `0s` is returned.
 *
 * @param time duration in seconds
 * @return the formatted duration string
 */
fun formatTime(time: Long): String {
    val days = time / 86400
    val hours = (time % 86400) / 3600
    val minutes = (time % 3600) / 60
    val seconds = time % 60

    val sb = StringBuilder()
    if (days > 0) sb.append(days).append("d")
    if (hours > 0) sb.append(hours).append("h")
    if (minutes > 0) sb.append(minutes).append("m")
    if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s")

    return sb.toString().trim { it <= ' ' }
}
/**
 * Parses a duration string into seconds. Supported formats:
 * a bare number (treated as minutes) or a number followed by a unit suffix:
 * `h` (hours), `d` (days), `w` (weeks), `m` (months), `y` (years).
 *
 * @param time the duration string to parse
 * @return the duration in seconds, `0` if `time` is `null`,
 *         or `-1` if the string is malformed
 */
fun parseTime(time: String?): Long {
    time ?: return 0
    var time = time
    if (time.isEmpty() || !Character.isDigit(time[0])) return -1
    val timeMod = time[time.length - 1].lowercaseChar() // last char

    if (Character.isDigit(timeMod)) {
        // minutes
        if (!Strings.canParseInt(time)) return -1
        return time.toLong() * 60
    }

    time = time.substring(0, time.length - 1)
    if (!Strings.canParseInt(time)) return -1

    val parsed = time.toLong()
    if (timeMod == 'h') return parsed * 60 * 60
    if (timeMod == 'd') return parsed * 60 * 60 * 24
    if (timeMod == 'w') return parsed * 60 * 60 * 24 * 7
    if (timeMod == 'm') return parsed * 60 * 60 * 24 * 30
    if (timeMod == 'y') return parsed * 60 * 60 * 24 * 365
    return parsed
}
/**
 * Resolves the player's UDP remote address as a string.
 *
 * @param player the player whose UDP address to retrieve
 * @return the UDP address string (without the leading `/`)
 */
fun getUDPAddress(player: Player): String {
    return Reflect.get<Connection>(player.con, "connection").remoteAddressUDP.address.toString().substring(1)
}

fun NetConnection.getUDPAddress(): InetAddress =
    Reflect.get<Connection>(this, "connection").remoteAddressUDP.address

/**
 * Checks whether the player has the given permission.
 *
 * @param perm the permission to check
 * @return `true` if the player has the permission, `false` otherwise
 */
fun Player.hasPerms(perm: Permission): Boolean {
    return getPerms(this).contains(perm)
}
/**
 * Finds a custom map whose name contains the given substring.
 *
 * @param name the substring to search for in map names
 * @return the first matching [Map], or `null` if none is found
 */
fun findMap(name: String): Map? {
    val maps = Vars.maps.customMaps()
    for (map in maps)
        if (map.name().contains(name))
            return map
    return null
}
/**
 * Subscribes to an event and invokes the listener on the async events scope.
 *
 * @param T the event type
 * @param type the event class to subscribe to
 * @param listener the listener invoked with the event
 */
fun <T> onAsync(type: Class<T>, listener: Cons<T>) {
    Events.on(type) { e: T ->
        eventsScope.launch {
            listener.get(e)
        }
    }
}

/**
 * Encodes a [BufferedImage] to a PNG [ByteArray].
 *
 * @param image the image to encode
 * @return the PNG-encoded bytes
 * @throws IOException if the image cannot be written
 */
@Throws(IOException::class)
fun parseImage(image: BufferedImage): ByteArray {
    val stream = ByteArrayOutputStream()
    ImageIO.write(image, "png", stream)
    return stream.toByteArray()
}

/**
 * Saves the current game to a file named `$name.$saveExtension`.
 *
 * @param name the save name
 * @return `false` if the game is not hosted, `true` otherwise
 */
fun save(name: String): Boolean {
    if (!Vars.state.isGame) {
        Log.err("Not hosting. Failed to save.")
        return false
    }

    val file = saveDirectory.child("$name.$saveExtension")

    Core.app.post {
        SaveIO.save(file)
        Log.info("Saved to @.", file)
    }
    return true
}

/**
 * Sends a localized message to the player.
 *
 * @param req the bundle key of the message
 */
fun Player.sendBundle(req: String) {
    Bundle.sendMessage(req, this)
}

/**
 * Sends a localized message with parameters to the player.
 *
 * @param req the bundle key of the message
 * @param params the format parameters
 */
fun Player.sendBundle(req: String, vararg params: Any) {
    Bundle.sendMessage(req, this, *params)
}

/**
 * Returns the total amount of players that have ever joined the server,
 * or the current player count if none is recorded.
 *
 * @return the player count
 */
fun getPlayersCount(): Int {
    return if (Core.settings.getInt("totalPlayers") == 0) Groups.player.size() else Core.settings.getInt("totalPlayers")
}

/**
 * Extracts the IDs from a list of Discord roles.
 *
 * @param roles the roles to process
 * @return the list of role IDs
 */
fun getRoleIDs(roles: List<Role>): List<String> {
    val list = ArrayList<String>()
    roles.forEach { role ->
        list.add(role.id)
    }
    return list
}

/**
 * Checks whether a Discord member has the role with the given ID.
 *
 * @param id the role ID to check
 * @return `true` if the member has the role, `false` otherwise
 */
fun Member.hasRole(id: String): Boolean {
    return getRoleIDs(this.roles).contains(id)
}


/**
 * Computes the unban timestamp for a ban of [seconds] seconds.
 *
 * @param seconds the ban duration in seconds, negative values mean a permanent ban
 * @return the unban [Instant], or `null` for a permanent ban
 */
fun getUnbanTime(seconds: Long): Instant? {
    if (seconds < 0) return null // perm ban

    return Clock.System.now() + seconds.seconds
}

/**
 * Removes color codes and escapes backslashes in a Discord message,
 * truncating it to 200 characters.
 *
 * @param message the message to sanitize
 * @return the sanitized message
 */
fun sanitizeDiscordMessage(message: String): String {
    return Strings.stripColors(message.replace("\\", "\\\\").take(200)).trim()
}

/**
 * Formats a Unix timestamp in milliseconds as a relative time string,
 * e.g. `3d ago`, `2h ago`, `5min ago`, `10sec ago`.
 *
 * @param time the timestamp in milliseconds
 * @return the relative time string
 */
fun formatAgo(time: Long): String {
    val diff = (System.currentTimeMillis() - time).coerceAtLeast(0)

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}min ago"
        else -> "${seconds}sec ago"
    }
}

/**
 * Converts a block's config value to a readable string:
 * - [UnlockableContent] is rendered as its emoji
 * - strings are returned as-is
 * - [Point2] and arrays of points are rendered as coordinates
 * - integers are resolved against the block (unit factory plan or power node link)
 * - [UnitCommand] is rendered as its emoji
 *
 * @param config the block config value
 * @param block the block the config belongs to
 * @return the readable config string, or `null` if it cannot be represented
 */
fun configAsString(config: Any?, block: Block): String? {
    var result = when (config) {
        is UnlockableContent -> config.emoji()

        is String -> config

        is Point2 -> config.toString()

        is Array<*> -> {
            val points = config.filterIsInstance<Point2>()

            if (points.isEmpty()) return null

            points.joinToString(prefix = "[", postfix = "]") {
                it.toString()
            }
        }

        is Int -> {
            if (block is UnitFactory) {
                val plans = block.plans
                if (config > plans.size) {
                    Log.err("config index out of bounds: config=$config, plansSize=${plans.size}")
                    return "[scarlet]ERR"
                }
                return plans[config].unit.emoji()
            } else if(block is PowerNode) {
                return Point2.unpack(config).toString()
            }
            null
        }

        is UnitCommand -> {
            return config.emoji.toString()
        }

        else -> null
    }

    if(result == null && block is UnitFactory) {
        result = "default"
    }

    return result
}

/**
 * Formats a Discord user ID as a mention.
 *
 * @return the mention string, e.g. `<@123456789>`
 */
fun Long.discordMention(): String {
    return "<@$this>"
}

fun InetAddress.toIntKey(): Int {
    val b = address // byte[4]
    return (b[0].toInt() and 0xFF shl 24) or
            (b[1].toInt() and 0xFF shl 16) or
            (b[2].toInt() and 0xFF shl 8) or
            (b[3].toInt() and 0xFF)
}