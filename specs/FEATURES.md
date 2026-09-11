# FEATURES.md

What each feature does, including the edge cases. Visual specifics live in `DESIGN.md`.

---

## 1. Feed

Built from subscribed channels' uploads playlists, merged and sorted by publish date. The algorithmic Home feed is not available through any API and is not attempted.

Filter chips: All, Unwatched, Under 10m, plus category chips. Chips are additive and persist across sessions.

Partially-watched items show a red progress bar on the thumbnail. Fully-watched items are dimmed to 60% opacity unless the Unwatched filter is active, which excludes them.

Pull to refresh. Cached content renders immediately; refresh happens underneath.

### Feed gestures

| Gesture | Result |
|---|---|
| Swipe right on a card | Save to Queue, with an inline undo row for 5s |
| Swipe left on a card | Hide from feed (local only, reversible in settings) |
| Long press | Context sheet: save to…, add tags, block channel, not interested |
| Tap | Open watch screen |

Swipe reveals a colored action panel that tracks the finger. Committing requires crossing 40% of card width; below that it springs back.

---

## 2. Watch screen

**Player pinned at top.** It does not scroll away.

**Two-tab segmented control**, full width, split 50/50 — Up next and Comments. Pinned directly beneath the player. Only the content below the tabs scrolls, so switching tabs is always one tap regardless of scroll depth.

**Description opens by tapping the title**, expanding inline beneath it — not a sheet, and it never covers the video. Contains: description text, tappable chapter list where each timestamp seeks, and extracted links. Tapping the title again collapses it. Timestamps inside description body text also become tappable chips.

### Player gestures

| Gesture | Result |
|---|---|
| Double tap left / right third | Seek ∓10s (interval configurable) |
| Single tap | Toggle controls |
| Vertical drag, left half | Brightness |
| Vertical drag, right half | Volume |
| Horizontal drag | Scrub with preview |
| Pinch | Fit / fill |
| Back | Minimize to floating mini-player |

Gesture guides appear only on first launch, never persistently.

### Action row

Save, audio-only, PiP, notes, speed. Five icon buttons. Save reflects current state — filled when the video is already in a collection.

### Dislike counts

Fetched from the Return YouTube Dislike API. **Always labelled as an estimate** — an `est.` chip beside the number and an attribution line beneath. YouTube removed real dislike data from the API in December 2021; these figures are extrapolated, accurate on large older videos and rough on small or new ones. Presenting them as fact would be wrong.

The like ratio bar is the more trustworthy signal and gets equal visual weight. If the API fails, hide the dislike element entirely rather than showing zero.

---

## 3. Notes and bookmarks

Bottom sheet from the watch screen. Each note captures the current playback timestamp automatically; the timestamp is editable. Tapping a note's timestamp seeks there.

Notes are searchable from the library and appear as their own result type there.

---

## 4. Library

Three collections: **Queue**, **Favorites**, **Archive**. Tabs, not folders — a video lives in exactly one collection at a time.

Each row shows: thumbnail with progress, title, tags, and either time remaining or duration.

Sort: date added, date published, duration, or manual drag order.

### Auto-archive

After N days unwatched (default off), items move from Queue to Archive. A banner shows what's due before it happens. Never deletes.

### Auto-remove watched

When enabled, an item is removed from Queue once `watch_state.completed` is true — defined as **≥90% watched** — after a **24-hour grace window**. The grace window matters: removing something the instant you pause near the end is infuriating and hard to undo.

Removal is soft — the item moves to Archive rather than vanishing — and an undo appears in the library for the session.

This applies only to the app's own Queue. YouTube's native Watch Later cannot be modified by any app; the API removed programmatic access to it in 2016. This is not a limitation to work around, it's a hard wall, and the custom Queue is the answer.

### Library search

One search field across saved titles, notes, and tags. Results are mixed-type: video rows and note rows in one list, with matched terms highlighted. Filter chips narrow to Titles, Notes, or Tags.

---

## 5. Blocked channels

A block button on any video card, channel page, or comment. Blocking is complete — the channel disappears from home, subscriptions, search results, up next, playlist views, and comments.

The Blocked settings screen lists every blocked channel with a one-tap unblock, and explicitly enumerates the surfaces the block applies to. Ambiguity about scope is worse than no feature.

**Sparse-result handling.** Because filtering is client-side, a search returning 20 results with 12 blocked leaves a thin page. Over-fetch and backfill to reach the target count, and show a "N results hidden — Show" affordance. Tapping Show reveals the hidden items inline for that session only.

---

## 6. Shorts

One setting, three modes:

| Mode | Behaviour |
|---|---|
| **Contained** (default) | Shorts appear only in the Shorts tab — never in home, subscriptions, search, or up next |
| **Off** | The Shorts tab is removed entirely; no Shorts anywhere |
| **Everywhere** | Mixed into feeds, like the official app |

Optional stricter detection: "treat vertical uploads as Shorts," default **off**, since it catches legitimate vertical videos.

Detection is the `/shorts/` URL path plus aspect ratio. Duration alone is unreliable — Shorts run up to 3 minutes.

**The Shorts tab is a grid**, not a vertical autoplay pager. Tapping opens the standard player. This is deliberate: containing Shorts and then rebuilding the infinite-swipe mechanic inside the container would defeat the purpose. If a swipe feed is wanted later, it should be scoped to the tapped result set, not endless.

**The bottom tab bar renders from a list, not a fixed set.** In Off mode it drops to three items and reflows — no gap where Shorts was.

---

## 7. Playlists

### Folders

Local-only grouping. Playlists can be filed into folders or left unfiled. Folders expand inline. YouTube has no concept of these; they exist entirely in `folders` and `playlists.folderId`.

### Progressive loading

Large playlists page in 50 at a time. The first page renders immediately and the rest streams in behind a progress banner showing "Loading N of M".

Search, sort, and filter work against whatever is cached, immediately — they don't wait for the full load. The banner disappears when complete. Subsequent visits are instant from cache.

### Within a playlist

- Search by title
- Sort: playlist order, date added, date published, duration, channel
- Filter: unwatched, duration ranges, channel
- All local queries against `playlist_items` — never API calls

---

## 8. Playback settings

**Ad handling.** With the recommended extraction approach, ad streams are simply never requested — there's nothing to block and no filter list to maintain. The WebView fallback path needs domain blocking plus cosmetic filtering and a filter list updater; see `ARCHITECTURE.md`.

**SponsorBlock.** Per-category toggles: Sponsor, Intro, Self-promo, Outro, Interaction. First three on by default. A brief toast on skip, with a tap-to-unskip. This uses the SponsorBlock community database and is unrelated to YouTube's own ad system.

**Quality per network.** Separate defaults for Wi-Fi and cellular.

**Background play**, **PiP**, **audio-only**.

**Per-channel speed memory.** When enabled, setting speed on a video stores it against that channel and applies to future videos from it.

---

## 9. Navigation and back behaviour

Back from a video minimizes to a **floating draggable mini-player** — thumbnail and red progress line only, no title text. Draggable to any corner, snaps on release. Close button in the corner; swipe to dismiss also works but the button is what makes it discoverable.

It needs a content description even though no title is drawn, or screen readers announce an unlabeled control.

Other required behaviours:

- Back from fullscreen returns to inline playback, not out of the video
- Back to the feed restores exact scroll position and active filter chips
- Predictive back gesture supported throughout
- Deep back stack never dumps to Home from three levels deep

---

## 10. Feed layout settings

Five layouts, selectable at runtime. All are arrangements of three card components — large card, compact row, grid tile — so no layout adds new components.

| Layout | Shape |
|---|---|
| Large | One per row, biggest thumbnails, ~2 per screen |
| Compact | Thumbnail left, text right, ~5 per screen |
| Hybrid | One large hero, then compact rows |
| Grid | Even tiles |
| Hybrid grid | One large hero, then grid tiles |

For Grid and Hybrid grid, a columns-per-row stepper appears. **Clamped 2–3 on phones** — at 360dp, 4-up gives ~76dp tiles where titles stop being legible. Default 2.

"More columns in landscape" toggle allows up to 5 when width permits. The stored setting is a preference at base width; actual column count is derived from available width at render time, so tablets and foldables work without a separate setting.

---

## 11. Entry points outside the app

**Home screen widget** — up-next queue items with resume state, plus a "play queue as audio" shortcut.

**Share sheet target** — receiving a YouTube link from any app opens a sheet showing the resolved video with collection picker and tag entry before saving.

---

## 12. Import / export

JSON export of all user data, CSV export of the saved list. Import is additive and idempotent — matching rows update rather than duplicate. Format in `DATA_MODEL.md`.

---

## Deliberately excluded

**Offline downloads.** Needs a download manager, storage settings, an offline tab, and eviction policy. It's a separate project. Revisit after everything above ships.

**Comments posting.** Read-only. Writing requires broader OAuth scopes for marginal benefit on a personal client.
