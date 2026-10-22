package org.echno.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.SiteTagShape

/** What a tag says about its item. The word on the tag carries the meaning; colour only backs it up. */
enum class SiteTagTone { Neutral, Navy, Amber, Green }

/**
 * A tag shaped like the ones tied to rebar on site: square end, punched hole, rounded tip.
 * Used for statuses ("Active", "Inactive") and for skills and certifications.
 */
@Composable
fun SiteTag(text: String, modifier: Modifier = Modifier, tone: SiteTagTone = SiteTagTone.Neutral) {
    val colors = MaterialTheme.colorScheme
    val (container, content) =
        when (tone) {
            SiteTagTone.Neutral -> colors.surfaceContainerLowest to colors.onSurface
            SiteTagTone.Navy -> colors.primary to colors.onPrimary
            SiteTagTone.Amber -> colors.secondary to colors.onSecondary
            SiteTagTone.Green -> colors.tertiary to colors.onTertiary
        }
    val neutral = tone == SiteTagTone.Neutral
    Surface(
        modifier = modifier,
        shape = SiteTagShape,
        color = container,
        contentColor = content,
        border = if (neutral) BorderStroke(1.dp, colors.outlineVariant) else null
    ) {
        Row(
            modifier = Modifier.heightIn(min = 32.dp).padding(start = 10.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The punched hole.
            Box(Modifier.size(6.dp).background(if (neutral) colors.outlineVariant else colors.surface, CircleShape))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (neutral) FontWeight.Medium else FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SiteTagPreview() {
    EchnoTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SiteTag("Active", tone = SiteTagTone.Navy)
            SiteTag("On Hold", tone = SiteTagTone.Amber)
            SiteTag("Completed", tone = SiteTagTone.Green)
            SiteTag("AutoCAD")
        }
    }
}
