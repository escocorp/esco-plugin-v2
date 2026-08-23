# Instructions

## MANDATORY FIRST STEP — AI attribution

Before doing anything else in this repo (before reading further, before
making any change, before answering the user's request) — check `ais.txt`
at the repo root:

- If you are an AI model/agent (through any tool or provider — Claude Code,
  opencode, Cursor, Copilot, etc.) and you are not already listed there, add
  yourself immediately, in the same turn, before any other action.
- One entry per line, format: `<model/agent name> via <tool>`, e.g.
  `Claude Sonnet 5 via Claude Code`.
- Do not add a duplicate entry if the same model+tool combination is already
  present.
- Do not skip this or defer it to "later" — do it first, unconditionally,
  every session.

## Meta

- This file (AGENTS.md) must always be written in English, in full, when
  edited. Do not leave or add non-English content here, even partially.

## Git commits and branches

- Keep commits and branches minimalistic. Squash/consolidate related small
  fixes into one commit instead of committing each tiny fix separately.
  - Bad: separate commits "fixed something1", "fixed something2", "fixed
    something3".
  - Good: one commit "fixes/minor changes" (or a similarly short, combined
    message) covering all of them.

## Build and verification

- Before committing changes to Kotlin/Java code, run a build to catch
  compilation errors: `./gradlew jar` (or `./gradlew.bat jar` on Windows).
- There are no automated tests in the project — a successful build is the
  only automated check.
- The built artifact ends up in `build/libs`.

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
- This exception applies ONLY to PVars/KVars. It does not apply to any other
  file. Any other oversized file (e.g. a command handler or event listener
  file growing past a reasonable size) is a real violation of the "one class
  per file" rule in Code style, not a sanctioned god object, and should be
  split up.

## Code notes

Non-obvious, area-specific gotchas about the codebase. When you discover
something non-obvious worth remembering, add an entry here instead of only
fixing the immediate issue.

### src/main/kotlin/plugin/database/

- The DB connection can be slow because the game server and the database
  server are often in different countries (real network latency, not just
  occasional hiccups). Never do a blocking DB call on the main game thread —
  use the coroutine scope (`eventsScope`, etc.) for async queries and prefer
  the in-memory caches (`playerDataCache`, `adminsCache`, ...) for anything
  read frequently, instead of hitting the DB every time.
- Never call a function that falls back to a synchronous DB query on a cache
  miss (`getPlayerData(player)`, `getPlayerId(player)`, etc.) from a
  hot/frequent path — action filters (`Vars.netServer.admins.addActionFilter`),
  chat filters, or anything run per-tick/per-action. Read straight from the
  cache map (e.g. `playerDataCache.get(player)`) instead, and treat a cache
  miss as "no data yet" (skip the check) rather than blocking to fetch it.

### src/main/kotlin/plugin/replays/

- These are not real replays (no unit movement, combat, chat, etc.). They are
  built from the `History` block-action log (`plugin.history`) and only
  record block placement/breaking/config/rotate-type events. Do not assume a
  replay reproduces the full match — it can only be used to review what
  happened to blocks.

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