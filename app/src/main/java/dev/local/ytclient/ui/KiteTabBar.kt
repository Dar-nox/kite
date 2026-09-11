package dev.local.ytclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.datastore.ShortsMode
import dev.local.ytclient.core.designsystem.ui.preview.KitePreviewEdgeToEdge
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * The bottom tab bar.
 *
 * Drawn from the list it is given, so a mode that removes a tab removes it here with no special
 * case. 0.5dp `borderSubtle` top edge, 18dp icons, 11sp labels, active white and inactive
 * `textTertiary`.
 */
@Composable
fun KiteTabBar(
    tabs: List<KiteTab>,
    selected: KiteTab,
    onSelect: (KiteTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .drawBehind {
                // Hairline top edge, drawn rather than a Divider so it stays 0.5dp at any density.
                drawLine(
                    color = colors.borderSubtle,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = KiteSize.hairline.toPx(),
                )
            }
            .height(KiteSize.tabBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            KiteTabBarItem(
                tab = tab,
                selected = tab == selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KiteTabBarItem(
    tab: KiteTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    // Never colour alone: the filled icon variant carries the active state alongside the tint.
    val tint = if (selected) colors.textPrimary else colors.textTertiary

    Column(
        modifier = modifier
            .height(KiteSize.tabBarHeight)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(KiteSize.tabIcon),
        )
        Spacer(Modifier.height(KiteSpacing.space2))
        Text(
            text = tab.label,
            style = AppTheme.type.micro,
            color = tint,
        )
    }
}

@Preview
@Composable
private fun KiteTabBarPreview() {
    KitePreviewEdgeToEdge {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            KiteTabBar(
                tabs = KiteTab.visibleFor(ShortsMode.Contained),
                selected = KiteTab.Home,
                onSelect = {},
            )
        }
    }
}
