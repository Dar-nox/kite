package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.preview.sampleVideoCard

/**
 * Hero card — the first item in Hybrid and Hybrid grid layouts.
 *
 * `DESIGN.md` specifies it as identical to the large card, so this delegates rather than
 * duplicating: a hero that drifts from the large card would be two components to keep in sync for
 * no visual difference. The separate name exists so feed code can say what it means at the call
 * site, and so a future deliberate difference has somewhere to live.
 */
@Composable
fun HeroVideoCard(
    data: VideoCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    VideoCardLarge(
        data = data,
        onClick = onClick,
        modifier = modifier,
        onLongClick = onLongClick,
    )
}

@Preview
@Composable
private fun HeroVideoCardPreview() {
    KitePreview {
        HeroVideoCard(data = sampleVideoCard, onClick = {})
    }
}
