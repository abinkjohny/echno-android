package org.echno.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme

/**
 * Pick one of a few short options ("Male · Female · Other") on a sunken track; the chosen one is
 * a raised white pill. TalkBack reads it as a group of radio buttons.
 */
@Composable
fun <T> ChoiceTrack(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(4.dp).selectableGroup().alpha(if (enabled) 1f else 0.6f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = { onSelect(option) }),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                        Text(
                            label(option),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun ChoiceTrackPreview() {
    EchnoTheme {
        ChoiceTrack(
            options = listOf("Full Day", "Morning", "Afternoon"),
            selected = "Full Day",
            onSelect = {},
            label = { it },
            modifier = Modifier.padding(16.dp)
        )
    }
}
