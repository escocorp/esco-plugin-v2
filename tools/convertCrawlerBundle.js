const fs = require("fs");

const file = "C:/Users/Vasil/IdeaProjects/esco-plugin-v2/src/plugin/gamemodes/crawlerarena/CrawlerArenaGamemode.java";

let text = fs.readFileSync(file, "utf8");

// Bundle.bundled(player, "key")
text = text.replace(
    /Bundle\.bundled\s*\(\s*([^,]+)\s*,\s*([^,)]+)\s*\)/g,
    'Bundle.sendMessage($2, $1)'
);

// Bundle.bundled(player, "key", ...)
text = text.replace(
    /Bundle\.bundled\s*\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^)]+)\)/g,
    'Bundle.sendMessage($2, $1, $3)'
);

// Bundle.sendToChat("key")
text = text.replace(
    /Bundle\.sendToChat\s*\(\s*([^,)]+)\s*\)/g,
    'Bundle.sendMessage($1)'
);

// Bundle.sendToChat("key", ...)
text = text.replace(
    /Bundle\.sendToChat\s*\(\s*([^,]+)\s*,\s*([^)]+)\)/g,
    'Bundle.sendMessage($1, $2)'
);

// Bundle.get(key, Bundle.findLocale(player))
text = text.replace(
    /Bundle\.get\s*\(\s*([^,]+)\s*,\s*Bundle\.findLocale\(([^)]+)\)\s*\)/g,
    'Bundle.get($1, $2.locale)'
);

// Bundle.format(key, Bundle.findLocale(player), ...)
text = text.replace(
    /Bundle\.format\s*\(\s*([^,]+)\s*,\s*Bundle\.findLocale\(([^)]+)\)\s*,\s*([^)]+)\)/g,
    'Bundle.get($1, $2.locale, $3)'
);

fs.writeFileSync(file, text);
console.log("Done");