package org.echno.feature.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.echno.BuildConfig
import org.echno.R
import org.echno.core.designsystem.component.EmptyState
import org.echno.core.designsystem.theme.EchnoTheme

/** Placeholder home screen; replaced by the project dashboard in Phase 4. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { innerPadding ->
        EmptyState(
            title = stringResource(R.string.home_placeholder_title),
            message = stringResource(R.string.home_placeholder_message, BuildConfig.FLAVOR, BuildConfig.API_BASE_URL),
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    EchnoTheme {
        HomeScreen()
    }
}
