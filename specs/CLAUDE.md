# CLAUDE.md

Project instructions for Claude Code. Read this first, then the doc relevant to the task.

## What this is

A personal, self-hosted Android YouTube client. Sideloaded only — not published to Play Store, single user, no analytics, no crash reporting, no telemetry of any kind.

It exists because the official app has three problems worth solving: features locked behind Premium that are trivial to implement locally, a UI that wastes vertical space and loses navigation state, and no way to permanently remove content you don't want to see.

## Documents

| File | Read it when |
|---|---|
| `ARCHITECTURE.md` | Choosing libraries, wiring modules, anything touching playback or networking |
| `DATA_MODEL.md` | Any Room entity, DAO, migration, or query |
| `FEATURES.md` | Implementing behaviour — what a feature does, its edge cases, its settings |
| `DESIGN.md` | Anything that draws pixels — colors, type, spacing, components, screen layout |
| `ROADMAP.md` | Deciding what to build next |

## Non-negotiables

**No telemetry.** No analytics SDK, no Firebase, no crash reporter, no network call that isn't fetching content the user asked for.

**Local-first.** Every feature works offline against cached data. The network enriches; it never gates. If the API is unreachable, the app still opens to a populated feed, a working library, and full playlists.

**Never block the UI on a network call.** Render cached data immediately, refresh in the background, reconcile when it lands.

**Filtering is never silent.** When blocked channels, Shorts containment, or keyword filters remove items from a list, the UI says so and offers a way to see what was hidden. A list that looks empty for unexplained reasons reads as a bug.

**Design tokens only.** No hardcoded hex, dp, or sp anywhere in feature code. Everything comes from the theme defined in `DESIGN.md`. If a value you need doesn't exist as a token, add it to the theme rather than inlining it.

## Conventions

- Kotlin, Jetpack Compose, single-activity
- Package root: `dev.local.ytclient`
- Repository pattern; ViewModels expose a single immutable `UiState` per screen
- Coroutines and Flow throughout; no RxJava, no LiveData
- Constructor injection via Hilt
- One Compose preview per component, dark theme only (the app has no light theme)
- Composables are stateless where possible; hoist state to the ViewModel

## Working style

Build vertically. A feature is done when its data layer, ViewModel, UI, and settings all work end to end — not when the UI renders with fake data. Prefer finishing one feature completely to scaffolding five.

When a spec here is ambiguous or looks wrong once you're in the code, say so and propose the fix rather than guessing silently.
