package dev.local.ytclient.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * Stand-in for a destination whose feature has not been built yet.
 *
 * States plainly what is missing instead of rendering an empty list that looks broken. Every use of
 * this is temporary and tracked in `specs/ROADMAP.md`; a destination keeps it only until its
 * feature lands, at which point the call site is replaced rather than filled in.
 */
@Composable
fun PendingScreen(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(KiteSpacing.screenMargin),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = AppTheme.type.sectionTitle,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(KiteSpacing.space8))
            Text(
                text = detail,
                style = AppTheme.type.body,
                color = colors.textSecondary,
            )
        }
    }
}
