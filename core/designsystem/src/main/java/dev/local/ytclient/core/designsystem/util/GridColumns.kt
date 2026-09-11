package dev.local.ytclient.core.designsystem.util

import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * Column counts for the Grid and Hybrid grid layouts.
 *
 * `FEATURES.md` is explicit that the *stored* setting is a preference at base width and the real
 * column count is derived from available width at render time — so a tablet or an unfolded foldable
 * gets more columns without a separate setting, and a narrow phone can never be pushed into
 * illegibility by a stale preference.
 *
 * The clamp is 2–3 on a phone: at 360dp, 4-up yields ~76dp tiles where titles stop being legible.
 * Landscape may go to 5, but only when the user has turned that on.
 */
object GridColumns {

    /** Smallest column count offered. One column would just be the Large layout. */
    const val MinPhone: Int = 2

    /** Largest column count allowed on a phone. */
    const val MaxPhone: Int = 3

    /** Largest column count allowed in landscape with "more columns in landscape" enabled. */
    const val MaxLandscape: Int = 5

    /** Default, per `DATA_MODEL.md`. */
    const val Default: Int = 2

    /**
     * Narrowest tile that still holds a two-line title at 13sp. Derived from the spec's own
     * example: 4-up at 360dp gives ~76dp and is called illegible, 3-up gives ~103dp and is fine.
     */
    const val MinTileWidthDp: Int = 100

    /**
     * Resolves the column count actually used to draw.
     *
     * @param requested the stored preference
     * @param availableWidthDp the width the grid will be laid out in, already minus nothing — screen
     *   margins are subtracted here
     * @param isLandscape current orientation
     * @param expandColumnsLandscape the "more columns in landscape" setting
     */
    fun resolve(
        requested: Int,
        availableWidthDp: Int,
        isLandscape: Boolean,
        expandColumnsLandscape: Boolean,
    ): Int {
        val cap = if (isLandscape && expandColumnsLandscape) MaxLandscape else MaxPhone
        val fitsWidth = maxForWidth(availableWidthDp)
        return requested
            .coerceIn(MinPhone, cap)
            .coerceAtMost(fitsWidth)
            .coerceAtLeast(MinPhone)
    }

    /**
     * How many [MinTileWidthDp] tiles fit the available width once screen margins and column gaps
     * are paid for. Never below [MinPhone], so a very narrow window degrades gracefully instead of
     * collapsing the grid.
     */
    fun maxForWidth(availableWidthDp: Int): Int {
        val margins = KiteSpacing.screenMargin.value.toInt() * 2
        val gap = KiteSpacing.gridColumnGap.value.toInt()
        val usable = availableWidthDp - margins + gap
        val perColumn = MinTileWidthDp + gap
        if (usable <= 0 || perColumn <= 0) return MinPhone
        return (usable / perColumn).coerceAtLeast(MinPhone)
    }

    /** Whether the columns stepper should appear: only for the two grid layouts. */
    fun isGridLayout(layoutName: String): Boolean =
        layoutName.equals("GRID", ignoreCase = true) ||
            layoutName.equals("HYBRID_GRID", ignoreCase = true)
}
