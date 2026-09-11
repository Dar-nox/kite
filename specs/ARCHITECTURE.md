# ARCHITECTURE.md

## The core decision: metadata and playback come from different places

This app has two separate jobs and they want different solutions.

**Metadata** — subscriptions, playlists, video details, search. The YouTube Data API v3 handles this well: structured JSON, stable contract, pagination, OAuth for the user's own subscriptions and playlists. Scraping this out of HTML would be miserable and would break constantly.

**Playback** — the actual video, with ad blocking, background audio, PiP, and custom gestures over the surface. The official embedded player cannot do any of those things.

So the app uses the Data API for metadata and a separate path for playback.

### Playback: recommended approach

Use a stream-extraction library (NewPipeExtractor or equivalent) to resolve a video ID to direct audio and video stream URLs, then play them with **Media3 / ExoPlayer**.

This is how NewPipe, LibreTube, and Grayjay work, and it's the only approach that delivers the feature set cleanly:

- No ads at all — ad streams are never requested, so there's nothing to block
- Real background playback via `MediaSessionService`, with proper notification controls
- Native PiP through Android's own API
- Full control over the player surface, so gestures are real touch handling rather than injected JavaScript
- Audio-only mode is free — just resolve the audio stream and skip video
- Playback speed, seek, and quality selection are native ExoPlayer features

**The cost:** extraction is unofficial and breaks when YouTube changes its internals — historically every few months. Budget for keeping the extractor dependency current. Isolate all of it behind a `StreamResolver` interface so a break is contained to one implementation class.

### Playback: fallback approach

A `WebView` loading the mobile site, with `shouldInterceptRequest` blocking known ad domains and injected JS hiding overlay elements. More resilient to extraction breakage, but background play and PiP become awkward, gestures have to be injected, and YouTube actively detects blockers on the web client. Keep this as a documented fallback, not the primary path.

### One honest note

Registering a Data API key means agreeing to YouTube's developer policies, which prohibit blocking ads in an API client. The realistic consequence for a personal, unpublished app is key revocation, not anything more serious — but use an API key tied to a throwaway Google account rather than one you depend on.

## Stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 with a fully custom dark theme |
| Navigation | Navigation Compose, single activity |
| DI | Hilt |
| Local storage | Room |
| Preferences | DataStore (Preferences) |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Images | Coil |
| Playback | Media3 / ExoPlayer + `MediaSessionService` |
| Stream resolution | NewPipeExtractor (isolated behind `StreamResolver`) |
| Background work | WorkManager |
| Sponsor segments | SponsorBlock public API |
| Dislike estimates | Return YouTube Dislike public API |

No Convex, no backend, no account system. Everything is on-device. If cross-device sync is ever wanted, it's an additive sync layer over the existing Room schema, not a rewrite — see the note at the end of `DATA_MODEL.md`.

## Modules

```
:app                    navigation graph, DI setup, MainActivity
:core:designsystem      theme, tokens, shared components (DESIGN.md)
:core:database          Room entities, DAOs, migrations (DATA_MODEL.md)
:core:datastore         settings
:core:network           Data API client, SponsorBlock, RYD
:core:player            StreamResolver, Media3 wrapper, MediaSessionService
:core:data              repositories, the filter pipeline, sync workers
:feature:feed           home feed, layout variants
:feature:watch          player screen, tabs, description, notes
:feature:library        queue / favorites / archive, search
:feature:playlists      folders, playlist detail, progressive loading
:feature:shorts         shorts grid tab
:feature:settings       all settings screens
```

Features depend on `:core:*` and never on each other. Anything shared between two features moves down into core.

## Data flow

Room is the single source of truth. The UI never reads from the network directly.

```
API / extractor  →  repository  →  Room  →  Flow  →  ViewModel  →  Compose
```

Screens observe Room via Flow, so a background sync writing to the database updates the UI automatically with no manual refresh path.

## The filter pipeline

Blocked channels, Shorts containment, and keyword filters are one composable pipeline applied at query time, not at insert time. Cached video rows stay pristine; filters are a view over them. That way toggling a setting takes effect instantly with no re-fetch, and unblocking a channel restores its videos rather than requiring a re-sync.

Every filtered list returns both the visible items and a count of what was removed, so the UI can show the "N hidden" affordance required by `CLAUDE.md`.

Apply filters in: home feed, subscriptions, search results, up next, playlist contents, and comment lists. The Shorts tab deliberately inverts the Shorts filter rather than skipping it.

## Sync

| Worker | Cadence | Does |
|---|---|---|
| `SubscriptionSyncWorker` | every 6h + on app open | refreshes subscribed channel list and uploads |
| `FeedRefreshWorker` | on app open, on pull-to-refresh | pulls recent uploads per subscribed channel |
| `PlaylistSyncWorker` | on demand, per playlist | pages through a playlist into Room, emits progress |
| `AutoArchiveWorker` | daily | archives and auto-removes per the rules in `FEATURES.md` |
| `FilterListWorker` | weekly | updates ad-domain filter list (WebView fallback only) |

## Quota

The Data API allows 10,000 units/day by default. A `list` call costs 1 unit, `search` costs 100 — avoid `search` where a cheaper endpoint works. A 2,000-item playlist sync costs 40 units (50 items per page), so even heavy use stays well inside the budget. Cache aggressively; never re-fetch a playlist page you already have.

## Error handling

Failures degrade rather than block. If extraction fails, show a clear message with a retry and a "open in browser" escape. If a metadata refresh fails, keep showing cached data with a quiet stale indicator. Never show an empty screen because a network call failed — there is almost always cached data to render instead.
