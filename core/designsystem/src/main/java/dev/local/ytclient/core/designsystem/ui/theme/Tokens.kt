package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, radius, and size tokens from `DESIGN.md`.
 *
 * These are plain objects rather than CompositionLocals: unlike colors they do not vary with theme,
 * and reading them without a composition keeps non-Composable helpers (measuring column counts,
 * building lists) simple. Same rule applies — no literal `dp` in feature code.
 */

/** 4dp base scale plus the semantic gaps the spec names. */
object KiteSpacing {
    // --- Base scale: 4, 8, 12, 16, 20, 24 ---
    /** Half-step, used only where the spec calls for a 2dp inset or badge padding. */
    val space2: Dp = 2.dp
    val space4: Dp = 4.dp
    val space8: Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp

    // --- Semantic ---
    /** Screen horizontal margin. */
    val screenMargin: Dp = 16.dp

    /** Between a card's thumbnail and its text. */
    val cardToText: Dp = 10.dp

    /** Between large feed items. */
    val feedItemGapLarge: Dp = 20.dp

    /** Between compact feed rows. */
    val feedItemGapCompact: Dp = 14.dp

    /** Between grid rows. */
    val feedItemGapGridRow: Dp = 14.dp

    /** Grid column gap. */
    val gridColumnGap: Dp = 10.dp

    /** Padding inside raised surfaces. */
    val insideSurface: Dp = 14.dp

    /** Section label to the content it labels. */
    val sectionLabelToContent: Dp = 10.dp

    /** Between unrelated raised surfaces (settings groups). */
    val surfaceGap: Dp = 8.dp

    /** Inner padding of a segmented control. */
    val segmentInset: Dp = 3.dp

    /** Chip padding: 12dp horizontal, 6dp vertical. */
    val chipHorizontal: Dp = 12.dp
    val chipVertical: Dp = 6.dp
}

/** Corner radii. The jump from YouTube's ~8dp to 16dp on large thumbnails is deliberate. */
object KiteRadius {
    /** Large thumbnail, hero card. */
    val thumbnailLarge: Dp = 16.dp

    /** Grid tile, compact thumbnail. */
    val thumbnailGrid: Dp = 12.dp

    /** Raised card, settings group, inline undo row. */
    val card: Dp = 14.dp

    /** Progress banner. */
    val banner: Dp = 12.dp

    /** Floating mini-player. */
    val miniPlayer: Dp = 12.dp

    /** Duration badge, tags, timestamp chips. */
    val badge: Dp = 6.dp

    /** Bottom sheet: 20dp top corners only, square bottom. */
    val sheetTop: Dp = 20.dp

    val sheetShape = RoundedCornerShape(topStart = sheetTop, topEnd = sheetTop)

    /** Pills, chips, buttons, segmented control and its selected segment. */
    val pillPercent = 50
}

/** Fixed component dimensions the spec calls out. */
object KiteSize {
    /** Channel avatar on a large card. */
    val avatarLargeCard: Dp = 28.dp

    /** Compact row thumbnail. */
    val compactThumbWidth: Dp = 88.dp
    val compactThumbHeight: Dp = 50.dp

    /** Toggle: 34x19 track, 15dp thumb, 2dp inset. */
    val toggleTrackWidth: Dp = 34.dp
    val toggleTrackHeight: Dp = 19.dp
    val toggleThumb: Dp = 15.dp
    val toggleInset: Dp = 2.dp

    /** Radio diameter. */
    val radio: Dp = 17.dp

    /** Leading icon in a settings row. */
    val leadingIcon: Dp = 16.dp

    /** Action-row icon buttons are visually 36dp inside a 48dp hit area. */
    val actionIcon: Dp = 36.dp
    val touchTarget: Dp = 48.dp

    /** Segmented control segment: 42dp inside a 3dp inset, giving a 48dp row. */
    val segmentHeight: Dp = 42.dp

    /** Progress bars. */
    val progressTrack: Dp = 3.dp
    val miniPlayerProgress: Dp = 2.5.dp

    /** Hairline borders. */
    val hairline: Dp = 0.5.dp
    val radioOutline: Dp = 1.5.dp

    /** Bottom tab bar. */
    val tabIcon: Dp = 18.dp

    /** Layout option row diagram. */
    val layoutDiagramWidth: Dp = 34.dp
    val layoutDiagramHeight: Dp = 30.dp

    /** Bottom sheet grab handle: 32x3dp, 12dp below. */
    val grabHandleWidth: Dp = 32.dp
    val grabHandleHeight: Dp = 3.dp
    val grabHandleGap: Dp = 12.dp

    /** Floating mini-player: 112dp wide at 16:9, 19dp close button, 16dp from edges. */
    val miniPlayerWidth: Dp = 112.dp
    val miniPlayerClose: Dp = 19.dp
    val miniPlayerEdgeInset: Dp = 16.dp
}

/** Aspect ratios as width/height floats, for `Modifier.aspectRatio`. */
object KiteAspect {
    const val Video = 16f / 9f
    const val Short = 9f / 16f
}
