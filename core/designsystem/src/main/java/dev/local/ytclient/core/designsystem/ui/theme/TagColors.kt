package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The tag palette from `DESIGN.md`: tags reference a [TagColorKey], never a raw hex.
 *
 * DESIGN.md names six keys (green, purple, blue, amber, coral, neutral) and separately defines a
 * surface/text pair for each family. This file makes that mapping explicit — each key reuses the
 * family pair the design doc already specifies, so no new hex values are invented:
 *
 * - green  -> `saveGreenSurface` / `saveGreenText`
 * - purple -> `timestampSurface` / `timestampText`
 * - blue   -> `infoBlue` surface stop / `infoBlueText`
 * - amber  -> `highlightSurface` / `highlightText`
 * - coral  -> `dangerSurface` / `dangerText`
 * - neutral-> `surfaceControl` / `textSecondary`
 *
 * The key is persisted in `tags.colorKey` (see `DATA_MODEL.md`), so the [persistedName] is the
 * stable wire value and must never be renamed without a migration.
 */
enum class TagColorKey(val persistedName: String) {
    Green("green"),
    Purple("purple"),
    Blue("blue"),
    Amber("amber"),
    Coral("coral"),
    Neutral("neutral");

    companion object {
        val Default: TagColorKey = Neutral

        /** Resolves a stored `colorKey`; unknown values fall back to [Default] rather than throwing. */
        fun fromPersistedName(value: String?): TagColorKey =
            entries.firstOrNull { it.persistedName.equals(value, ignoreCase = true) } ?: Default
    }
}

/** Surface/text pair drawn behind a tag chip. Text always uses the family's light stop. */
@Immutable
data class TagColors(
    val surface: Color,
    val text: Color,
)

/**
 * Full palette, resolved once. Feature code calls [AppColors.tagColors] with a [TagColorKey].
 */
@Immutable
val TagPalette: Map<TagColorKey, TagColors> = mapOf(
    TagColorKey.Green to TagColors(surface = SaveGreenSurface, text = SaveGreenText),
    TagColorKey.Purple to TagColors(surface = TimestampSurface, text = TimestampText),
    TagColorKey.Blue to TagColors(surface = InfoBlue, text = InfoBlueText),
    TagColorKey.Amber to TagColors(surface = HighlightSurface, text = HighlightText),
    TagColorKey.Coral to TagColors(surface = DangerSurface, text = DangerText),
    TagColorKey.Neutral to TagColors(surface = SurfaceControl, text = TextSecondary),
)
