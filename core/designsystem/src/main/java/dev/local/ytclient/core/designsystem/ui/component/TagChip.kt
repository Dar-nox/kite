package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.model.TagChipData
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.preview.sampleTags
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing
import androidx.compose.foundation.clickable

/**
 * Tag chip: colored surface and text resolved from the tag's `colorKey`, `micro` type, 6dp radius.
 *
 * Text always uses the family's light stop — never white, never gray — which is why the color pair
 * comes from the palette rather than being chosen at the call site.
 */
@Composable
fun TagChip(
    tag: TagChipData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors.tagColors(tag.colorKey)
    val clickable = if (onClick != null) {
        Modifier.clickable(onClickLabel = "Show $tag", role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Text(
        text = tag.name,
        style = AppTheme.type.micro,
        color = colors.text,
        modifier = modifier
            .clip(RoundedCornerShape(KiteRadius.badge))
            .background(colors.surface)
            .then(clickable)
            .padding(horizontal = KiteSpacing.space8, vertical = KiteSpacing.space2),
    )
}

@Preview
@Composable
private fun TagChipPreview() {
    KitePreview {
        TagChip(tag = sampleTags.first())
    }
}
