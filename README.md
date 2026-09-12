# kite

A personal, self-hosted Android YouTube client. Sideloaded, single user, no telemetry of any kind.
Built from the specs in [`specs/`](specs) — read [`specs/CLAUDE.md`](specs/CLAUDE.md) first.

## Status

Phases 0 and 1 of [`specs/ROADMAP.md`](specs/ROADMAP.md) are in: the module graph, the full theme and
token set, every shared component, the complete Room schema, settings, the Data API client, and a
feed that syncs real uploads and survives being offline.

| Built | Not yet |
|---|---|
| Gradle multi-module scaffold, Hilt wiring | Playback, `StreamResolver`, Media3 (phase 2) |
| Design system: every token from `DESIGN.md` | Saving, library, notes (phase 3) |
| All Phase 0 components, one dark preview each | Channel blocking UI, keyword filters, Shorts tab (phase 4) |
| Room schema + DAOs for all of `DATA_MODEL.md` | Playlists, folders, progressive loading (phase 5) |
| Settings DataStore, every key | Remaining feed layouts, mini-player, SponsorBlock (phase 6) |
| Query-time filter pipeline | Widget, share target, import/export (phase 7) |
| Feed: data layer → ViewModel → screen | |
| Data API client, uploads sync, workers | |

### Two known gaps in phase 1

**No Google sign-in.** `ARCHITECTURE.md` calls for OAuth to read the account's own subscriptions.
What is built instead is the seam for it — `AccountAuthenticator`, bound to
`KeyOnlyAccountAuthenticator` — plus subscribing by pasted URL, handle, or channel id, which works on
the API key alone and populates the same feed. Swapping in real sign-in is a DI binding, an OAuth
client id, and a credential-manager dependency; nothing in the repositories changes. I did not ship a
Sign-In integration I could not run once, because a wrong scope or client type fails in a way that is
miserable to diagnose from the outside.

**Shorts detection is incomplete.** The Data API exposes no `/shorts/` path and no true aspect ratio,
and duration alone is ruled out by `FEATURES.md`. So metadata sync leaves `isShort` false and the
stream resolver in phase 2 writes it back from the watch page. Containment is exact for anything the
extractor has seen and approximate before that.

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

The YouTube Data API key is read at build time from an untracked `secrets.properties` at the repo
root — copy `secrets.properties.example` and fill in `YOUTUBE_API_KEY`. A build without one compiles
and runs; API calls fail as `ApiFailure.MissingKey` and the app serves cached data.

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

