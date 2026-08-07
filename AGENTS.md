# Instructions

## Code style

- Do not add pointless comments that explain basic code. For example, avoid
  `val x = 10 // assign x to 10` and similar.

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