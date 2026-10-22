package org.echno.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.R
import org.echno.core.designsystem.theme.EchnoTheme

// Full-screen placeholders shared by every list/detail screen, so loading, empty and
// error states look the same across features. Each sits in a white card on the paper ground,
// no wider than a phone so it stays compact on tablets.

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** Nothing to show: the crane drawing, what's missing, and what to expect. [action] is an optional way on. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: (@Composable ColumnScope.() -> Unit)? = null
) {
    StateCard(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.illustration_crane), contentDescription = null)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (message != null) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        action?.invoke(this)
    }
}

/** Something went wrong: what failed ([title]), why ([message]), and a way to try again. */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_title_default)
) {
    // A live region, so TalkBack announces the failure when it replaces the loading spinner.
    StateCard(modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_warning), contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Text(message, style = MaterialTheme.typography.bodyMedium)
        FilledButton(stringResource(R.string.action_retry), onClick = onRetry, modifier = Modifier.fillMaxWidth(), height = 52.dp)
    }
}

@Composable
private fun StateCard(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    // Scrolls if the text is enlarged far enough not to fit.
    Box(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun EmptyStatePreview() {
    EchnoTheme {
        EmptyState(title = "No projects yet", message = "Projects you are added to will appear here.")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun ErrorStatePreview() {
    EchnoTheme {
        ErrorState(title = "Couldn't load your profile", message = "Can't reach Echno. Check your internet connection.", onRetry = {})
    }
}
