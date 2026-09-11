package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale from `DESIGN.md`. System font, two weights only — 400 and 500.
 *
 * Heavier weights are banned by the spec because they read as shouting on dark backgrounds, so
 * there is no bold token here and feature code has nothing to reach for. Nothing below 11sp.
 *
 * Line height: 1.35 on the title tokens, 1.45 on the body-side tokens (body, label, meta, micro).
 * DESIGN.md splits the scale that way without naming every token, so the split is stated here once
 * rather than guessed per call site.
 */
@Immutable
data class KiteType(
    /** Screen headers. 18sp / 500. */
    val screenTitle: TextStyle,
    /** In-screen section headers. 16sp / 500. */
    val sectionTitle: TextStyle,
    /** Large card and hero titles. 15sp / 400. */
    val cardTitleLarge: TextStyle,
    /** Body copy, comments, notes. 14sp / 400. */
    val body: TextStyle,
    /** Compact rows, grid tiles. 13sp / 400. */
    val cardTitle: TextStyle,
    /** Settings labels, tabs. 13sp / 400. */
    val label: TextStyle,
    /** Channel, views, dates, durations. 12sp / 400. */
    val meta: TextStyle,
    /** Chips, badges, helper text. 11sp / 400 — the floor. */
    val micro: TextStyle,
)

private val TitleLineHeight = 1.35f
private val BodyLineHeight = 1.45f

val KiteTypography: KiteType = KiteType(
    screenTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * TitleLineHeight).sp,
    ),
    sectionTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = (16 * TitleLineHeight).sp,
    ),
    cardTitleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * TitleLineHeight).sp,
    ),
    body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * BodyLineHeight).sp,
    ),
    cardTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = (13 * TitleLineHeight).sp,
    ),
    label = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = (13 * BodyLineHeight).sp,
    ),
    meta = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * BodyLineHeight).sp,
    ),
    micro = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = (11 * BodyLineHeight).sp,
    ),
)

val LocalKiteType = staticCompositionLocalOf { KiteTypography }
