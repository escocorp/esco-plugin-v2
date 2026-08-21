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

## Changelog style

Changelogs are written for Discord and target players, not developers. One
change per line, no blank lines between entries.

Each line starts with an emoji tag, then ` - `, then a short present-tense
description, then a commit link:

- `🆕` for new features/additions.
- `🛠️` for changes, fixes, reworks, and removals.

Line format:

```
<emoji> - <description> ([<short-hash>](<https://github.com/escocorp/esco-plugin-v2/commit/<full-hash>>))
```

Notes:

- The commit URL is wrapped in `<...>` (inside the markdown link parentheses)
  so Discord does not render a link preview.
- Use the short hash as the link text and the full 40-char hash in the URL.
- Use backticks for commands/identifiers, e.g. `` `/top` ``.
- Reference Discord channels with `<#channelId>` when relevant.
- A line starting with `-# ` is a Discord subtext note (small text). Use it for
  sub-details attached to the entry above it (examples, clarifications).
- Optionally prefix a section/author header line with `###`, e.g.
  `### kukoldiki`.
- Group multiple related changes from the same commit as separate lines that
  all link to that same commit; do not invent per-feature hashes.
- Describe only player-visible changes. Skip internal-only refactors (package
  moves, event-timing tweaks, `.gitignore`, etc.) unless the changelog is
  explicitly technical.

Example:

```
🆕 - Added Arras.io gamemode ([7a39acb](<https://github.com/escocorp/esco-plugin-v2/commit/7a39acb0d31658d49d9b39436d0e1ea5c92c1246>))
🆕 - Added `/top` — balance leaderboard with a menu to pick the top type ([7a39acb](<https://github.com/escocorp/esco-plugin-v2/commit/7a39acb0d31658d49d9b39436d0e1ea5c92c1246>))
🛠️ - Fixed duplicate commands in `/help` and the Foo's command list ([7ecf00d](<https://github.com/escocorp/esco-plugin-v2/commit/7ecf00d43ec966df918a107493dc43a44b540e5b>))
-# /ban - `command` | %ban - `dscommand` | votekick - `votekick`, etc.
```