# kite

A personal, self-hosted Android YouTube client. Sideloaded, single user, no telemetry of any kind.
Built from the specs in [`specs/`](specs) — read [`specs/CLAUDE.md`](specs/CLAUDE.md) first.

## Status

Phase 0 of [`specs/ROADMAP.md`](specs/ROADMAP.md) is in: the module graph, the full theme and token
set, every shared component, the complete Room schema, settings, and an app that launches to a real
(empty) feed.

| Built | Not yet |
|---|---|
| Gradle multi-module scaffold, Hilt wiring | Data API client, OAuth, sync workers (phase 1) |
| Design system: every token from `DESIGN.md` | Playback, `StreamResolver`, Media3 (phase 2) |
| All Phase 0 components, one dark preview each | Saving, library, notes (phase 3) |
| Room schema + DAOs for all of `DATA_MODEL.md` | Channel blocking UI, keyword filters, Shorts tab (phase 4) |
| Settings DataStore, every key | Playlists, folders, progressive loading (phase 5) |
| Query-time filter pipeline | Remaining feed layouts, mini-player, SponsorBlock (phase 6) |
| Feed: data layer → ViewModel → screen | Widget, share target, import/export (phase 7) |

The destinations that have no feature yet render a `PendingScreen` saying which phase they land in,
rather than an empty list that looks broken.

## Building

Requires JDK 17 and the Android SDK (compileSdk 36).

```bash
./gradlew assembleDebug     # or open the folder in Android Studio
./gradlew test              # unit tests
```

`gradle/wrapper/gradle-wrapper.jar` is not committed — this repo was scaffolded in a sandbox with no
access to Gradle's distribution servers. Either let Android Studio generate the wrapper, or run
`gradle wrapper --gradle-version 8.14.3` once locally. The pinned versions in
`gradle/wrapper/gradle-wrapper.properties` and `gradle/libs.versions.toml` are otherwise complete.

The YouTube Data API key is not in source control. It arrives with the network module in phase 1 and
will be read from an untracked `secrets.properties`.

## Checks

```bash
pip install tree-sitter tree-sitter-kotlin   # once
python3 scripts/check.py
```

Kotlin syntax, XML resource resolution, spec-to-theme colour conformance, design-token references,
and the "no hardcoded hex/dp/sp in feature code" rule. It is **not** a type check — there is no JVM
here, so `./gradlew build` is still the real gate. See the script's docstring for exactly what it
does and does not cover.

## Layout

```
:app                    navigation graph, DI setup, MainActivity, tab bar
:core:designsystem      tokens, theme, shared components
:core:database          Room entities, DAOs, migrations
:core:datastore         settings
:core:network           Data API client, SponsorBlock, RYD          (empty until phase 1)
:core:player            StreamResolver, Media3, MediaSessionService  (empty until phase 2)
:core:data              repositories, the filter pipeline, sync workers
:feature:*              feed, watch, library, playlists, shorts, settings
```

Features depend on `:core:*` and never on each other.

## Spec amendments

Five problems found while implementing. All are now fixed in `specs/` itself rather than left as
code-only deviations, so the next phase builds against one contract:

- **No database-level foreign keys onto cache tables.** `DATA_MODEL.md` annotated
  `saved_items.videoId` as `FK → videos` while also requiring that cached rows be wipeable without
  losing user data — a cascading key deletes the saves, a non-cascading one makes the clear fail.
  The design-rules section now states the rule, and the entity tables say "indexed reference".
- **`hidden_videos` table.** Swipe-left-to-hide and "not interested" had nowhere to live; a column on
  `videos` would be lost or resurrected by a cache refresh.
- **Three settings keys the features need** had no entry: `feed.filters`, `player.seekIntervalSec`,
  `watch.showGestureGuides`. Added to the settings list.
- **`videos.categoryId`.** The feed's category chips need it and the table didn't have it. Added
  while the schema is version 1 with no rows — after the first sync it would cost a migration and a
  migration test. The chips themselves still land in phase 4.
- **`saved_items` unique index** was on (`videoId`, `collection`), which permits a video in two
  collections; `FEATURES.md` says one at a time. Now unique on `videoId` alone, so the database
  enforces the invariant instead of the DAO hoping to.

Also clarified in `DESIGN.md`: which type tokens take 1.35 line height and which take 1.45.

