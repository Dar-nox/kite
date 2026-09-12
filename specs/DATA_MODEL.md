# DATA_MODEL.md

Room schema. All timestamps are epoch millis (`Long`). All durations and positions are seconds (`Int`).

## Design rules

- Cached content and user data are separate. Cached rows (`videos`, `channels`, `playlist_items`) can be wiped and re-fetched without losing anything the user created.
- User data (`saved_items`, `notes`, `tags`, `folders`, `watch_state`, block lists) is never deleted by a cache clear and is what gets exported.
- Filtering is a query concern, never a stored one. Blocked or hidden content stays in `videos`; it's excluded at read time.
- **No database-level foreign keys onto a cache table.** The two rules above cannot both hold with one: a cascading key onto `videos` would delete the user's saves when the cache is cleared, and a non-cascading one would make the clear fail on a constraint. So `videos.channelId`, `saved_items.videoId`, `notes.videoId`, `watch_state.videoId`, and `playlist_items.videoId` are indexed columns whose integrity the repository maintains — it upserts a minimal `videos` row whenever it writes user data for a video. Keys between two user-data tables (`saved_item_tags`) are real and cascade.

## Entities

### `videos` — cached metadata

| Column | Type | Notes |
|---|---|---|
| `videoId` | String | PK |
| `title` | String | |
| `channelId` | String | indexed reference → `channels`, not a DB-level FK |
| `durationSec` | Int | |
| `publishedAt` | Long | |
| `thumbnailUrl` | String | highest available |
| `viewCount` | Long? | |
| `isShort` | Boolean | `/shorts/` path or vertical aspect |
| `isVertical` | Boolean | aspect < 1.0, used by the optional stricter Shorts rule |
| `description` | String? | fetched lazily on watch |
| `categoryId` | String? | YouTube's numeric category id, for the feed's category chips |
| `cachedAt` | Long | |

Index on `channelId`, `publishedAt`.

### `channels`

| Column | Type | Notes |
|---|---|---|
| `channelId` | String | PK |
| `title` | String | |
| `avatarUrl` | String? | |
| `uploadsPlaylistId` | String? | source for the feed |
| `subscriberCount` | Long? | |
| `isSubscribed` | Boolean | |
| `isBlocked` | Boolean | |
| `defaultSpeed` | Float? | null = use global |
| `autoSaveTarget` | String? | null = off, else a collection name |
| `autoSaveTags` | String? | comma-separated tag ids |

### `saved_items` — the custom Watch Later

| Column | Type | Notes |
|---|---|---|
| `id` | Long | PK autogenerate |
| `videoId` | String | indexed reference → `videos`, not a DB-level FK |
| `collection` | String | `QUEUE`, `FAVORITES`, `ARCHIVE` |
| `addedAt` | Long | |
| `sortOrder` | Int | manual reordering |
| `autoAdded` | Boolean | true if a rule added it |

Unique index on `videoId` alone — not (`videoId`, `collection`). A video lives in exactly one
collection at a time, and a composite index would permit the same video in two of them. Moving
between collections is a delete-plus-insert inside one transaction.

### `watch_state`

| Column | Type | Notes |
|---|---|---|
| `videoId` | String | PK |
| `positionSec` | Int | |
| `durationSec` | Int | |
| `completed` | Boolean | set at ≥90% watched |
| `completedAt` | Long? | starts the auto-remove grace window |
| `lastWatchedAt` | Long | |

This is the app's own history, deliberately separate from the Google account's.

### `notes`

| Column | Type | Notes |
|---|---|---|
| `id` | Long | PK autogenerate |
| `videoId` | String | FK → `videos` |
| `timestampSec` | Int? | null = a note about the whole video |
| `body` | String | |
| `createdAt` | Long | |

FTS4 virtual table over `body` for library search.

### `tags` / `saved_item_tags`

`tags`: `id` (PK), `name` (unique), `colorKey` (references a palette entry in `DESIGN.md`).

`saved_item_tags`: `savedItemId`, `tagId`, composite PK. Many-to-many.

### `playlists`

| Column | Type | Notes |
|---|---|---|
| `playlistId` | String | PK |
| `title` | String | |
| `thumbnailUrl` | String? | |
| `itemCount` | Int | reported by the API |
| `cachedItemCount` | Int | how many rows are actually local |
| `folderId` | Long? | FK → `folders`, null = unfiled |
| `lastSyncedAt` | Long? | |
| `sortOrder` | Int | within its folder |

`cachedItemCount` vs `itemCount` drives the progressive-loading banner.

### `playlist_items`

| Column | Type | Notes |
|---|---|---|
| `playlistId` | String | FK, composite PK with `position` |
| `position` | Int | |
| `videoId` | String | FK → `videos` |
| `addedAt` | Long? | |

Index on `playlistId`. Sorting and filtering inside a playlist are local queries against this join — never API calls.

### `folders`

`id` (PK), `name`, `sortOrder`. Purely local; YouTube has no equivalent.

### `keyword_filters`

`id` (PK), `term`, `matchTitle` (Boolean), `matchDescription` (Boolean).

### `hidden_videos`

`id` (PK), `videoId` (unique index), `hiddenAt` (Long).

Feed swipe-left and "not interested" need somewhere to live, and it is not a column on `videos`: a
cache refresh would either resurrect the item or lose the user's choice. Local only, reversible from
settings, and excluded by the filter pipeline at read time like everything else.

## Settings (DataStore, not Room)

```
feed.layout                 LARGE | COMPACT | HYBRID | GRID | HYBRID_GRID
feed.gridColumns            2..3 on phones, clamped at read time
feed.expandColumnsLandscape Boolean
feed.filters                Set<String> (active chips; they persist across sessions)
shorts.mode                 CONTAINED | OFF | EVERYWHERE
shorts.treatVerticalAsShort Boolean (default false)
player.backgroundPlay       Boolean
player.rememberSpeedPerChannel Boolean
player.defaultSpeed         Float
player.seekIntervalSec      Int (default 10; double-tap seek distance, 5..30)
player.qualityWifi          String
player.qualityCellular      String
watch.tabDefault            UP_NEXT | COMMENTS
watch.showGestureGuides     Boolean (default true; guides show on first launch only)
library.autoArchiveDays     Int (0 = off)
library.autoRemoveWatched   Boolean
library.autoRemoveGraceHours Int (default 24)
sponsorblock.enabled        Boolean
sponsorblock.categories     Set<String>
dislikes.showEstimates      Boolean
```

## Export format

Export writes user data only, as JSON:

```json
{
  "version": 1,
  "exportedAt": 0,
  "savedItems": [{ "videoId": "", "collection": "", "addedAt": 0, "tags": [] }],
  "notes": [{ "videoId": "", "timestampSec": 0, "body": "", "createdAt": 0 }],
  "watchState": [{ "videoId": "", "positionSec": 0, "completed": false }],
  "blockedChannels": ["channelId"],
  "keywordFilters": ["term"],
  "folders": [{ "name": "", "playlistIds": [] }],
  "channelRules": [{ "channelId": "", "autoSaveTarget": "", "defaultSpeed": 1.0 }]
}
```

Import is additive and idempotent: existing rows are matched on natural key and updated, never duplicated. CSV export of `savedItems` only is offered as a convenience.

## On future sync

If cross-device sync is ever added, every user-data table already has a stable natural key and an `addedAt`/`createdAt` timestamp, which is enough for last-write-wins reconciliation. No schema change would be required — only a sync layer above the DAOs. Don't build it now.
