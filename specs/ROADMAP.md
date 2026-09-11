# ROADMAP.md

Build order. Each phase produces something usable — no phase ends with scaffolding that doesn't run.

---

## Phase 0 — Foundation

Project setup, module structure per `ARCHITECTURE.md`, Hilt wiring, Room database with the full schema from `DATA_MODEL.md`, DataStore settings, and the complete theme and token set from `DESIGN.md`.

Also build the shared components now, with previews: video card in all three variants, chip, segmented control, settings row, toggle, radio, stepper, timestamp chip, tag chip.

**Done when:** the app launches to an empty feed, the theme is applied, and every component renders in preview.

---

## Phase 1 — Metadata and feed

Data API client, OAuth, subscription sync, uploads-playlist merge into Room. Feed screen reading from Room via Flow, with the Large layout only.

**Done when:** signing in populates a real feed that survives being offline.

---

## Phase 2 — Playback

`StreamResolver` behind its interface, Media3 wrapper, `MediaSessionService`. Watch screen with the pinned player, pinned segmented control, up next, comments, and the inline description with tappable chapters.

Player gestures: double-tap seek, vertical drags, scrub.

**Done when:** videos play with working gestures and no ads.

---

## Phase 3 — Saving and the library

`saved_items`, `watch_state`, collections, tags. Feed swipe gestures with the inline undo row. Library screen with collections, sort, and the auto-archive banner.

Notes sheet with timestamped bookmarks.

**Done when:** you can save from the feed, organize in the library, and take notes on a video.

---

## Phase 4 — Filtering

The filter pipeline as a query-time concern. Channel blocking with full surface coverage and the hidden-results affordance. Keyword filters. Shorts containment with all three modes, plus the Shorts grid tab and the conditional tab bar.

**Done when:** blocking a channel removes it everywhere, and Shorts stay in their tab.

---

## Phase 5 — Playlists

Folders, playlist sync with progressive loading and its banner, playlist detail with local search, sort, and filter.

**Done when:** a 2,000-item playlist opens instantly and is searchable while still loading.

---

## Phase 6 — Polish and settings

Remaining feed layouts — Compact, Hybrid, Grid, Hybrid grid — plus the layout settings screen with the vertical option rows, the columns stepper, and width-derived column counts.

Floating mini-player and the full back-navigation behaviour. SponsorBlock. Dislike estimates. Per-channel speed. Quality per network. Auto-remove watched with its grace window.

**Done when:** everything in `FEATURES.md` works and every setting has a UI.

---

## Phase 7 — Outside the app

Home screen widget, share sheet target, JSON and CSV import/export.

---

## Later, if ever

Offline downloads. Cross-device sync. A scoped vertical Shorts pager. None of these are needed for the app to be complete — treat them as separate projects.

---

## Sequencing notes

Phases 1 and 2 are the risky ones. If stream extraction turns out to be unworkable, that's known before any UI investment, and the WebView fallback in `ARCHITECTURE.md` becomes the path.

Phase 4 depends on Phase 1's schema but not on Phase 3, so it can move earlier if the feed feels noisy while building.

Don't start Phase 6's layout variants until one layout is fully correct. Five variants of a card that isn't right yet is five times the rework.
