package org.echno.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.SectionNumberTextStyle
import org.echno.core.designsystem.theme.SectionTitleTextStyle

/**
 * "01 CONTACT": a section title numbered like the sheets of a drawing, over a hairline rule.
 * TalkBack reads it as a heading, without the number.
 */
@Composable
fun SectionHeader(number: Int, title: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp).semantics(mergeDescendants = true) {
                heading()
                contentDescription = title
            },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text("%02d".format(number), style = SectionNumberTextStyle, color = EchnoTheme.colors.amberText)
            Text(title.toUpperCase(Locale.current), style = SectionTitleTextStyle, color = MaterialTheme.colorScheme.onSurface)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** A white card on the paper ground. Rows inside it are separated by [DetailRow]'s rule. */
@Composable
fun DetailCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(Modifier.padding(horizontal = 16.dp), content = content)
    }
}

/**
 * A label above its value, with an optional action on the right ("Call", "Email").
 * [showRule] draws the hairline under the row; leave it off on a card's last row.
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showRule: Boolean = false,
    action: (@Composable RowScope.() -> Unit)? = null
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledValue(label, value, Modifier.weight(1f))
            action?.invoke(this)
        }
        if (showRule) HorizontalDivider(color = EchnoTheme.colors.cardRule)
    }
}

/** A small label above a 17sp value: the building block of detail rows and grids. */
@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** The pill-shaped action at the end of a detail row. [description] is what TalkBack reads, e.g. "Call +91 98470 12345". */
@Composable
fun RowActionButton(text: String, description: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 40.dp).semantics { contentDescription = description },
        shape = MaterialTheme.shapes.extraLarge,
        colors =
        ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun SectionPreview() {
    EchnoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader(1, "Contact")
            DetailCard {
                DetailRow("Phone", "+91 98470 12345", showRule = true) {
                    RowActionButton("Call", "Call +91 98470 12345", onClick = {})
                }
                DetailRow("Blood group", "B+")
            }
        }
    }
}
