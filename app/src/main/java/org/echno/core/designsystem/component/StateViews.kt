package org.echno.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.R
import org.echno.core.designsystem.theme.EchnoTheme

// Full-screen placeholders shared by every list/detail screen, so loading, empty and
// error states look the same across features.

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(title: String, modifier: Modifier = Modifier, message: String? = null) {
    CenteredColumn(modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (message != null) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun CenteredColumn(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    EchnoTheme {
        EmptyState(title = "No projects yet", message = "Projects you are added to will appear here.")
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    EchnoTheme {
        ErrorState(message = "Couldn't reach the server.", onRetry = {})
    }
}
