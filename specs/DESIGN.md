# DESIGN.md

Dark theme only. The app has no light theme and should not attempt one.

## Direction

Keep YouTube's palette so it feels familiar, then fix what makes it feel dated: red used everywhere, cramped 8dp corners, avatars repeated at every level, and no elevation vocabulary.

Three rules carry most of the difference:

1. **Red is an accent, not a brand wash.** It appears on progress indicators and the app mark. Nowhere else. Subscribe buttons, badges, and active states use white-on-dark inversion instead.
2. **Generous corners.** 16dp on large thumbnails against YouTube's ~8dp. This is the single most visible modernization.
3. **A real elevation scale.** Background, raised surface, control surface — three distinct tiers, so grouped content reads as grouped instead of floating on one flat plane.

---

## Color

Define as a Compose `darkColorScheme` extension plus a custom `LocalAppColors`. Never inline a hex value in feature code.

### Surfaces

| Token | Hex | Use |
|---|---|---|
| `background` | `#0F0F0F` | app background |
| `surfaceRaised` | `#1C1C1F` | cards, sheets, settings rows, grouped content |
| `surfaceControl` | `#262626` | unselected chips, secondary buttons |
| `surfaceInverse` | `#FFFFFF` | selected chips, primary buttons |

### Borders

| Token | Hex | Use |
|---|---|---|
| `borderSubtle` | `#1F1F22` | section dividers, tab bar top edge |
| `borderDefault` | `#2A2A2E` | inside raised surfaces |
| `borderStrong` | `#3A3A3D` | unselected radio outlines, toggle tracks |

### Text

| Token | Hex | Use |
|---|---|---|
| `textPrimary` | `#FFFFFF` | titles, primary values |
| `textBody` | `#DDDDDD` | body copy on raised surfaces |
| `textSecondary` | `#9A9A9A` | metadata — channel, views, dates |
| `textTertiary` | `#7A7A7A` | section labels, captions, helper text |
| `textInverse` | `#0F0F0F` | on `surfaceInverse` |

### Accent and semantic

| Token | Hex | Use |
|---|---|---|
| `accentRed` | `#FF0033` | progress bars, app mark — **nothing else** |
| `saveGreen` | `#1D9E75` | toggle-on fill, swipe-save panel |
| `saveGreenText` | `#9FE1CB` | text/icon on green surfaces |
| `saveGreenSurface` | `#1F3A2E` | green tag backgrounds |
| `infoBlue` | `#378ADD` | selected radio fill, progress banner |
| `infoBlueText` | `#78A9FF` | inline actions — Undo, Show, links |
| `timestampSurface` | `#2E2438` | timestamp and chapter chips |
| `timestampText` | `#CECBF6` | text on those chips |
| `dangerSurface` | `#3A2424` | keyword filter chips |
| `dangerText` | `#F7C1C1` | text on danger surfaces |
| `warningAmber` | `#FAC775` | folder icons, pending-action banners |
| `highlightSurface` | `#4A3F1A` | search match highlight background |
| `highlightText` | `#FAC775` | search match highlight text |

**Text on colored surfaces always uses that family's light stop.** Never white, never gray.

### Tag palette

Tags reference a `colorKey`, not a raw hex. Available: green, purple, blue, amber, coral, neutral. Each has a surface and text pair following the pattern above.

---

## Type

System font (Roboto). Two weights only: **400 regular** and **500 medium**. Never 600 or 700 — heavier weights read as shouting on dark backgrounds.

| Token | Size | Weight | Use |
|---|---|---|---|
| `screenTitle` | 18sp | 500 | screen headers |
| `sectionTitle` | 16sp | 500 | in-screen section headers |
| `cardTitleLarge` | 15sp | 400 | large card and hero titles |
| `body` | 14sp | 400 | body copy, comments, notes |
| `cardTitle` | 13sp | 400 | compact rows, grid tiles |
| `label` | 13sp | 400 | settings labels, tabs |
| `meta` | 12sp | 400 | channel, views, dates, durations |
| `micro` | 11sp | 400 | chips, badges, helper text |

Line height 1.35 for the title tokens (`screenTitle`, `sectionTitle`, `cardTitleLarge`,
`cardTitle`), 1.45 for the body-side tokens (`body`, `label`, `meta`, `micro`). Never below 11sp
anywhere.

**Sentence case everywhere.** No Title Case, no ALL CAPS, including section labels.

### Title clamping

| Context | Lines |
|---|---|
| Large card | 2 |
| Compact row | 2 |
| Grid tile | **exactly 2** with ellipsis |
| Hero card | 2 |
| Mini-player | none — no title drawn |

Grid tiles at 2 lines is a hard requirement. One line truncates most real titles into uselessness.

---

## Spacing and shape

4dp base scale: 4, 8, 12, 16, 20, 24.

| Context | Value |
|---|---|
| Screen horizontal margin | 16dp |
| Between card and its text | 10dp |
| Between feed items | 20dp (large), 14dp (compact), 14dp (grid rows) |
| Grid column gap | 10dp |
| Inside raised surfaces | 14dp |
| Section label to content | 10dp |

### Radius

| Element | Radius |
|---|---|
| Large thumbnail | 16dp |
| Grid tile / compact thumbnail | 12dp |
| Raised card / settings group | 14dp |
| Pills, chips, buttons | fully rounded |
| Bottom sheet | 20dp top corners only |
| Mini-player | 12dp |
| Duration badge, tags | 6dp |

### Touch targets

Minimum 48dp, extended with padding where the visual element is smaller. Icon buttons in the action row are visually 36dp with a 48dp hit area.

---

## Components

### Video card — large
Thumbnail 16:9, 16dp radius. Duration badge bottom-right, `rgba(0,0,0,0.8)` at 6dp radius. Progress bar 3dp along the bottom edge, red on `rgba(255,255,255,0.25)`, only when partially watched. Below: 28dp channel avatar, then title (2 lines) and metadata line.

### Video card — compact
88×50dp thumbnail left, 12dp radius. Title 2 lines, metadata below. **No avatar** — at this size it's noise. Progress bar as above.

### Video card — grid tile
Thumbnail fills column width at 16:9, 12dp radius. Title exactly 2 lines. Channel name plus view count as text only, **no avatar**.

### Hero card
Identical to the large card. Used as the first item in Hybrid and Hybrid grid layouts.

### Chip
Fully rounded, 12dp horizontal padding, 6dp vertical. Selected: `surfaceInverse` background, `textInverse` text. Unselected: `surfaceControl` background, `#EEEEEE` text.

### Segmented control
Full width, `surfaceRaised` background, 3dp inner padding, fully rounded. Segments split evenly. Selected segment: `surfaceInverse` fill, fully rounded, `textInverse` text. Used for the watch screen's two tabs.

### Settings row
`surfaceRaised`, 14dp radius, 14dp padding. Leading icon 16dp in `textBody`. Label in `label` token. Trailing: toggle, value text, chevron, or stepper. Optional helper line in `textTertiary` at `micro` beneath.

Consecutive rows in a group share one surface with dividers; unrelated rows are separate surfaces with 8dp between.

### Toggle
34×19dp track, 15dp thumb, 2dp inset. On: `saveGreen` track, white thumb right. Off: `borderStrong` track, `#8A8A8E` thumb left.

### Radio
17dp. Selected: `infoBlue` fill with white check. Unselected: 1.5dp `borderStrong` outline, transparent.

### Stepper
Pill on `background` inside a raised row. Minus, value, plus. Disabled bound renders at `textTertiary` rather than hiding.

### Timestamp chip
`timestampSurface` background, `timestampText` text, `micro`, 6dp radius. Used for chapters and note timestamps.

### Tag chip
Colored surface and text from the tag's `colorKey`, `micro`, 6dp radius.

### Inline undo row
`surfaceRaised`, 14dp radius, check icon in `saveGreenText`, message in `textBody`, "Undo" in `infoBlueText`. Appears in flow, pushing content down — not a floating snackbar. Auto-dismisses at 5s.

### Progress banner
`surfaceRaised`, 12dp radius. Refresh icon in `infoBlueText`, "Loading N of M" in `textTertiary`, percentage right-aligned. 3dp track beneath, `infoBlue` fill on `borderDefault`.

### Hidden-results notice
`surfaceRaised`, eye-off icon in `textTertiary`, "N results hidden" in `textSecondary`, "Show" in `infoBlueText`.

### Layout option row
Settings-row shaped. Leading 34×30dp diagram built from `borderStrong` blocks showing the arrangement. Then name and one-line description. Trailing radio.

### Bottom sheet
`surfaceRaised`, 20dp top corners. 32×3dp grab handle centered with 12dp below. Content padding 14dp.

### Floating mini-player
112dp wide, 16:9, 12dp radius, 0.5dp `borderStrong` outline. Contains **only** the thumbnail, a 2.5dp red progress line at the bottom, and a 19dp close button top-right on `rgba(0,0,0,0.65)`. No title. Draggable, snaps to nearest corner, 16dp from edges, clears the tab bar. Requires a content description.

### Bottom tab bar
Renders from a list, not a fixed set — Shorts Off mode drops it to three items and reflows. 0.5dp `borderSubtle` top edge. Icons 18dp, labels 11sp. Active white, inactive `textTertiary`.

---

## Screen layouts

### Feed
Header with title and search. Filter chips row, horizontally scrollable. Content per selected layout. Pull to refresh.

### Watch
1. Player, pinned, 16:9
2. Title (2 lines) with expand chevron — tap expands description inline
3. Metadata line
4. Channel row: avatar, name, subscribe button
5. Action row: 5 icon buttons on `surfaceRaised` pills
6. Segmented control — Up next / Comments — **pinned beneath the player**
7. Scrolling content

Only 7 scrolls. The player and segmented control stay fixed.

Expanded description sits between 3 and 5: `surfaceRaised` card containing description text, a chapter list of timestamp chips plus titles, and links in `infoBlueText`.

### Library
Header, collection chips with counts, search field, item rows, auto-archive banner at the bottom when relevant.

### Playlists
Folder rows expanding inline to nested playlist rows. Unfiled section last.

### Playlist detail
Back header with title and "N videos · N hours". Search field. Sort and filter chips. Progress banner if syncing. Item rows.

### Shorts tab
Header, source chips (Subscriptions / Saved), 3-column grid of 9:16 tiles with play count overlay and one-line caption.

### Settings
Grouped raised surfaces under `textTertiary` section labels. Feed layout gets its own screen — the five options as **equal-width vertical rows**, never a grid. Five never divides evenly into rows, and spanning the odd one falsely implies it's featured. The columns stepper and landscape toggle appear below, only when a grid layout is selected.

---

## Motion

| Interaction | Duration | Curve |
|---|---|---|
| Chip / tab selection | 150ms | ease-out |
| Sheet in / out | 250ms | ease-out |
| Description expand | 200ms | ease-out |
| Swipe action commit | 200ms | ease-out |
| Swipe spring-back | 150ms | ease-out |
| Mini-player corner snap | 250ms | spring, low bounce |
| Layout change | 300ms | ease-in-out |

Respect the system reduced-motion setting: cross-fade instead of translating, keep durations, never remove feedback entirely.

---

## Accessibility

- Content descriptions on every icon-only control, including the mini-player
- 48dp minimum touch targets
- Text contrast at or above 4.5:1 — `textTertiary` on `background` is the floor, don't go lighter
- Respect system font scaling; test at 1.3× where grid tiles are tightest
- Gesture actions have a non-gesture equivalent in the long-press context sheet
- Never use color alone to convey state — pair with icon or text
