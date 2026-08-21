# Instructions

## Tooling

- Prefer the IntelliJ IDEA MCP server (`intellij-index`) for code intelligence
  whenever it is available: finding files/classes/symbols, go to definition,
  find references, call/type hierarchies, implementations, and reading
  library/dependency sources (arc, Mindustry, JDA, JDK, etc. via `jar://` paths).
  It is more accurate than plain text search and can navigate into dependencies.
  - Plugin: https://plugins.jetbrains.com/plugin/29174-ide-index-mcp-server
- If the MCP server is not available, fall back to the normal tools (Grep, Glob,
  Read, etc.).

## Code style

- Do not add pointless comments that explain basic code. For example, avoid
  `val x = 10 // assign x to 10` and similar.
- KDoc/Javadoc comments are only allowed on functions. Do not document
  classes or fields (properties, constants, etc.).
- Put new classes/data classes and their related helper functions in their own
  dedicated file, named after the class. Do not append them to unrelated
  existing files (e.g. do not add a `BalanceEntry` class and its query helper
  to `PlayerData.kt`; create `BalanceEntry.kt` instead).

## Emojis

- Do not use standard emojis in messages sent to players in-game. The game
  client only renders the game's internal emojis.
- Emojis are allowed in Discord messages.

## Concurrency

- The Mindustry server is synchronous, so race conditions are usually not
  possible. They can only happen if the code runs on another thread, via Kotlin
  coroutines, and similar.

## God objects

- God objects like PVars/KVars are used intentionally because the game's
  developer (Anuke) also has such an object: `mindustry.Vars`.

## Localization

- Any player-facing messages (`menu`, `sendMessage`, `infoMessage`, etc.) must
  be sent through bundles and localized.
- Make sure the submodules are initialized and the messages are translated to
  the corresponding languages.

### Translation files

- All translations live in: `esco-plugin-v2/src/main/resources/bundles`
- All available languages are listed in: `esco-plugin-v2/src/main/resources/locales`

Locale files must look like this:

```
key=value;
```

Example:

```
something=line1
line2
line3;
settings.label=Settings menu;
```

`;` marks the end of a key and the transition to the next key.