/*
 https://github.com/space-syndicate/Goob-Station/blob/28e40bdd0bea3b38deeda10019a6e2f8ae5225e4/Content.Goobstation.Server/Speech/OhioAccentSystem.cs#L9
https://github.com/space-syndicate/Goob-Station/blob/28e40bdd0bea3b38deeda10019a6e2f8ae5225e4/Resources/Locale/ru-RU/_Goobstation/accent/ohio.ftl#L11
https://github.com/space-syndicate/Goob-Station/blob/28e40bdd0bea3b38deeda10019a6e2f8ae5225e4/Resources/Locale/en-US/_Goobstation/accent/ohio.ftl#L11
 */

package plugin.chat

import plugin.PVars

private const val PREFIX_CHANCE = 0.3f
private const val SUFFIX_CHANCE = 0.4f

private val enPrefixes = listOf(
    "Gyatt dang,", "Chat...", "Epic win,", "Widewawwy...",
    "BRO...", "Call me the rizzler cause,", "It's giving..."
)

private val enSuffixes = listOf(
    ". Like in Ohio.", ". From Ohio...", ". Like in Fortnite.", ". Like from Fortnite.",
    ". For the Rizzler.", ". Chat is this real?", ". Bro knew what he was doing.",
    ". Goofy ahh.", ". Like erm... what the sigma???", ". What the scallop?",
    ". It's so over.", ". I oop!!!!!!11!!!111!", ". I need to work on my mewing."
)

private val ruPrefixes = listOf(
    "О кринге,", "Чатик...", "Эпик вин,", "Буквальна...",
    "БРАТАН...", "Зови меня сигмой, потому что", "Это вайб...",
    "Порошка белого хочется...", "Ооо чёрт...", "Щас бы порошка белого занюхнуть..."
)

private val ruSuffixes = listOf(
    ". Как в Саратове.", ". Из Саратова...", ". Как в Фортлайт.", ". Как из Фортлайта.",
    ". Для Сигмы.", ". Чатик, это реально?", ". Братан знал, что делает.",
    ". Кринжовый.", ". Типа эмм... что за сигма???", ". Что за дичь?",
    ". Это такой конец.", ". Ой, я падаю!!!!!!11!!!111!", ". Мне надо поработать над клоунингом.",
    ". Я район держу этот."
)

private val enWords = mapOf(
    "charisma" to "rizz", "cool" to "sigma", "amazing" to "rizzlike", "god" to "gyatt",
    "attack" to "unalive", "kill" to "unalive", "murder" to "unalive", "dead" to "in ohio",
    "maints" to "the backrooms", "maintenance" to "the backrooms", "maint" to "the backrooms",
    "attacked" to "unalived", "nukie" to "sussy baka impostor from Among Us",
    "syndicate" to "sussy baka impostor from Among Us", "syndi" to "sussy baka impostor from Among Us",
    "traitor" to "sussy baka impostor from Among Us", "got" to "gyatt", "delicious" to "bussin'",
    "yummy" to "bussin'", "women" to "FEMALES", "girls" to "FEMALES", "girl" to "FEMALE",
    "woman" to "FEMALE", "miss" to "FEMALE", "ms" to "FEMALE", "mrs" to "FEMALE",
    "bitch" to "FEMALE", "really" to "for real", "definitely" to "lowkey", "mhm" to "on god",
    "epic" to "poggers", "lingium" to "ligma", "game" to "roblox", "nah" to "cope",
    "weird" to "sus", "brother" to "bro", "man" to "bro", "marijuana" to "420 leaf",
    "weed" to "420 leaf", "best" to "GOAT", "loss" to "L", "lose" to "take an L",
    "lost" to "took an L", "silly" to "goofy ahh", "clown" to "goofy ahh", "funny" to "goofy",
    "joke" to "meme", "idiot" to "baka", "ugly" to "rizzless", "smartass" to "nerd",
    "smart" to "nerdlike", "science" to "nerdland", "scientist" to "professional nerd",
    "story" to "lorepage", "loser" to "L + Ratio idiot", "nice" to "rizzlike",
    "spesos" to "rizzbucks", "dollars" to "rizzbucks", "dollar" to "rizzbuck",
    "speso" to "rizzbuck", "money" to "rizzbucks", "kill you" to "send you to Brazil",
    "dick" to "glizzy", "hot dog" to "glizzy", "butt" to "bussy", "bum" to "bussy",
    "ass" to "bussy", "kill yourself" to "send yourself to Brazil you stupid rizzless citizen of Ohio",
    "felinid" to "hecking chonker", "cat" to "hecking chonker", "kitty" to "hecking chonker",
    "ian" to "hecking chonker", "dog" to "hecking chonker", "cerberus" to "hecking chonker",
    "puppy" to "hecking chonker", "pup" to "hecking chonker", "tesla" to "sparkly rizzball",
    "singularity" to "sussy singuawungoose", "singu" to "sussy singuawungoose",
    "singulo" to "sussy singuawungoose", "tesloose" to "SPARKLY RIZZBALL LOOSE NO CAP",
    "tesla loose" to "SPARKLY RIZZBALL LOOSE NO CAP", "hacking" to "hacking like in a video game",
    "robust" to "cooking", "die" to "get unalived", "died" to "was unalived",
    "goddamn" to "gyattdamn", "godamn" to "gyattdamn", "goddamned" to "gyatdamned",
    "goddang" to "gyattdang", "fuck" to "skibidi", "shit" to "skibidi", "im high" to "im tweaking",
    "i'm high" to "i'm tweaking", "supermatter" to "fanum crystal", "erping" to "going to freaky town",
    "erp" to "freaky", "sm" to "fanum crystal", "changeling" to "shapeshifting ohioan",
    "cling" to "shapeshifting ohioan", "heretic" to "facebook crystal worshipper",
    "heretics" to "members of a crystal-worshipping facebook group", "news" to "fake news",
    "tax" to "fanum tax", "cool guy" to "real sigma alpha male guy", "fed" to "fanum taxer",
    "athlete" to "ishowspeed", "meth" to "speed", "chemistry" to "walter white",
    "chem" to "walter white", "real news" to "fake news",
    "important" to "important like paying your fanum taxes", "literally" to "widewawwy",
    "best friend" to "bestie", "caught" to "caught in 4k", "delusional" to "delulu",
    "toes" to "dogs", "boss" to "girlboss", "make-over" to "glow-up", "makeover" to "glowup",
    "make over" to "glow up", "greatest" to "goat", "gross" to "icky",
    "pun pun" to "ipad-addicted monkey", "security" to "karen department", "secoff" to "pig",
    "hos" to "donut-feasting karen", "rumor" to "tea", "throw" to "yeet", "gay" to "zesty",
    "tajaran" to "hecking chonker"
)

private val ruWords = mapOf(
    "харизма" to "сигмо", "круто" to "сигма", "замечательно" to "сигмачайше",
    "бог" to "я не верующий", "атаковать" to "антиоживить", "убить" to "антиоживить",
    "ликвидировать" to "антиоживить", "мертв" to "в Саратове", "техи" to "бэкрумс",
    "техтуннели" to "бэкрумс", "технические туннели" to "бэкрумс", "атакован" to "антиоживлён",
    "опер" to "импостер в броне", "синдикат" to "импостеры", "синди" to "импостер",
    "агент" to "импостер", "получил" to "спиздил", "изумительно" to "вкуснятинка",
    "вкусно" to "вкуснятинка", "женщина" to "ЖЕНЩИНА", "девушки" to "ЖЕНЩИНЫ",
    "девушка" to "ЖЕНЩИНА", "мисс" to "ЖЕНЩИНА", "миссис" to "ЖЕНЩИНА", "мэм" to "ЖЕНЩИНА",
    "мадам" to "ЖЕНЩИНА", "мадмазель" to "ЖЕНЩИНА", "сука" to "ЖЕНЩИНА", "серьёзно" to "реально",
    "точно" to "ловки", "мгм" to "о кринге", "эпически" to "сигмачайше", "литий" to "лигма",
    "игра" to "боблокс", "не" to "не пизди", "странно" to "сас", "брат" to "братан",
    "мужик" to "анк", "марихуана" to "220 лист", "курево" to "220 лист", "лучший" to "ГОАТ",
    "потеря" to "Л", "потерять" to "словить Л", "потерял" to "словил Л", "глупый" to "кринжовый",
    "клоун" to "кринжовик", "смешной" to "кринжовый", "шутка" to "мем", "еблан" to "балбес",
    "уродливый" to "несигма", "душнила" to "душнилыч", "умный" to "задротистый",
    "наука" to "задротленд", "ученый" to "профессиональный задрот", "история" to "лор",
    "лох" to "слит", "хорошо" to "сигморошо", "кредиты" to "сигмакредиты",
    "доллары" to "сигмадоллары", "доллар" to "сигмадоллар", "кредит" to "сигмакредит",
    "деньги" to "сигмакредиты", "убью тебя" to "отправлю в Саратов", "мудак" to "эль гандонио",
    "хот дог" to "сосисон", "задница" to "попка", "попа" to "попка", "зад" to "попка",
    "убей себя" to "отправь себя в Саратов сигмолишённый житель Челябинска",
    "феленид" to "анимешник", "кот" to "мем котик", "кошка" to "мем котик",
    "иан" to "милый пухляш", "собака" to "милый пухляш", "цербер" to "трехглавый пухляш",
    "щенок" to "милый пухляш", "щеночек" to "милый пухляш", "тесла" to "искрящийся сигмашар",
    "сингулярность" to "суси сингошар", "сингу" to "суси сингошар", "сингуло" to "суси сингошар",
    "теслалуз" to "ИСКРЯЩИЙСЯ СИГМАШАР СБЕЖАЛ", "тесла луз" to "ИСКРЯЩИЙСЯ СИГМАШАР СБЕЖАЛ",
    "взлом" to "хак как в видеоигре", "робаст" to "кукед", "умереть" to "антиоживиться",
    "умер" to "антиоживился", "проклятье" to "о кринге!", "охуеть" to "обалдеть",
    "чертов" to "очумелый", "да чтоб тебя" to "да ну нафиг", "блять" to "скибиди",
    "дерьмо" to "скибиди", "отдыхаю" to "чилю", "отдыхать" to "чилить",
    "суперматерия" to "весёлый кристал", "ерпшить" to "сексится", "ерп" to "похоть",
    "см" to "весёлый кристал", "генокрад" to "изменяющийся саратовец",
    "генка" to "изменяющийся саратовец", "еретик" to "участник битвы экстрасенсов",
    "еретики" to "участники битвы экстрасенсов", "новость" to "фейк", "налог" to "весёлый налог",
    "крутой чувак" to "настоящий сигма альфач", "федерал" to "весёлый налоговик",
    "атлет" to "айшоусдвиг", "мет" to "спиды", "химия" to "Колтер Байт", "химка" to "Колтер Байт",
    "настоящая новость" to "фейк ньюз", "важно" to "важно так же как заплатить налоги",
    "буквально" to "буквальна", "лучший друг" to "друзьяшка", "пойман" to "пойман в 4к",
    "бред" to "кринж", "стопы" to "тяги", "босс" to "биг босс", "преобразиться" to "апгрейднуться",
    "преображение" to "апгрейд", "преображения" to "апгрейды", "величайший" to "гоат",
    "мерзость" to "фу бе", "пун пун" to "кпкзависимая макака", "сб" to "отдел хряков",
    "офицер" to "хряк", "гсб" to "всепожирающий хряк", "информация" to "инфа",
    "кинуть" to "кикнуть", "гей" to "пидорюга", "таяра" to "максвел", "диломет" to "спиды",
    "новости" to "фейки", "рнд" to "задротленд", "нио" to "задротленд",
    "тебя убью" to "отправлю в Саратов"
)

private val enPatterns = compile(enWords)
private val ruPatterns = compile(ruWords)

private fun compile(words: Map<String, String>): List<Pair<Regex, String>> =
    words.entries
        .sortedByDescending { it.key.length }
        .map { (word, repl) ->
            Regex("(?<!\\p{L})" + Regex.escape(word) + "(?!\\p{L})", RegexOption.IGNORE_CASE) to repl
        }

fun ohioify(message: String, locale: String?): String {
    if (message.isBlank()) return message

    val russian = locale != null && (locale.startsWith("ru") || locale.startsWith("uk"))

    var text = applyReplacements(message, if (russian) ruPatterns else enPatterns)

    if (PVars.random.nextFloat() < PREFIX_CHANCE) {
        val prefixes = if (russian) ruPrefixes else enPrefixes
        text = prefixes[PVars.random.nextInt(prefixes.size)] + " " + text.replaceFirstChar { it.lowercase() }
    }

    text = text.replaceFirstChar { it.uppercase() }

    if (PVars.random.nextFloat() < SUFFIX_CHANCE) {
        val suffixes = if (russian) ruSuffixes else enSuffixes
        text += suffixes[PVars.random.nextInt(suffixes.size)]
    }

    return text
}

private fun applyReplacements(message: String, patterns: List<Pair<Regex, String>>): String {
    var text = message
    for ((regex, repl) in patterns) {
        text = regex.replace(text, Regex.escapeReplacement(repl))
    }
    return text
}
