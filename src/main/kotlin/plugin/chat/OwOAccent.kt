// https://github.com/space-syndicate/space-station-14/blob/d4bad3abbd80a5e68587d70ccfc5277a49c7945b/Content.Shared/Speech/EntitySystems/OwOAccentSystem.cs#L9
// this file translated from C#

package plugin.chat

import plugin.PVars

private val faces = listOf(" (•`ω´•)", " ;;w;;", " owo", " UwU", " >w<", " ^w^")

private val specialWords =
    linkedMapOf(
        "you" to "wu",
        "ты" to "ти",
    )

private val colorTag = Regex("\\[[^\\[\\]]*]")

fun owoify(message: String): String {
    var text = message
    for ((word, repl) in specialWords) {
        text = text.replace(word, repl)
    }

    val tags = colorTag.findAll(text).map { it.value }.iterator()
    return colorTag.split(text).joinToString(separator = "") { segment ->
        accentSegment(segment) + (if (tags.hasNext()) tags.next() else "")
    }
}

private fun accentSegment(segment: String): String =
    segment
        .replace("!", faces[PVars.random.nextInt(faces.size)])
        .replace("r", "w")
        .replace("R", "W")
        .replace("l", "w")
        .replace("L", "W")
        .replace("р", "в")
        .replace("Р", "В")
        .replace("л", "в")
        .replace("Л", "В")
