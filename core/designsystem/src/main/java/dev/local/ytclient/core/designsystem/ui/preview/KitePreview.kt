package dev.local.ytclient.core.designsystem.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing
import dev.local.ytclient.core.designsystem.ui.theme.KiteTheme

/**
 * Wrapper for the single preview each component carries.
 *
 * `DESIGN.md`/`CLAUDE.md` fix the app to dark theme only, so previews never take a theme argument —
 * there is exactly one way to look at a component. [padding] defaults to the screen margin so
 * previews show components at their real inset.
 */
@Composable
fun KitePreview(
    padding: Dp = KiteSpacing.screenMargin,
    content: @Composable () -> Unit,
) {
    KiteTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(padding),
        ) {
            content()
        }
    }
}

/** Padding-free variant for full-bleed components such as the mini-player or the tab bar. */
@Composable
fun KitePreviewEdgeToEdge(content: @Composable () -> Unit) = KitePreview(padding = 0.dp, content = content)
