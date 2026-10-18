package org.echno.feature.organization.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.echno.R
import org.echno.core.designsystem.component.ErrorState
import org.echno.core.designsystem.component.LoadingState
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.messageRes
import org.echno.feature.organization.domain.Organization

@Composable
fun OrganizationPickerScreen(modifier: Modifier = Modifier, viewModel: OrganizationPickerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OrganizationPickerContent(
        uiState = uiState,
        onSelect = viewModel::select,
        onRetry = viewModel::retry,
        onSignOut = viewModel::signOut,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationPickerContent(
    uiState: OrganizationPickerUiState,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.organization_picker_title)) },
                actions = { TextButton(onClick = onSignOut) { Text(stringResource(R.string.action_sign_out)) } }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            OrganizationPickerUiState.Loading -> LoadingState(contentModifier)
            is OrganizationPickerUiState.Error ->
                ErrorState(stringResource(uiState.error.messageRes()), onRetry = onRetry, modifier = contentModifier)
            is OrganizationPickerUiState.Loaded ->
                if (uiState.organizations.isEmpty()) {
                    NoOrganization(onRetry, onSignOut, contentModifier)
                } else {
                    OrganizationList(uiState.organizations, onSelect, contentModifier)
                }
        }
    }
}

@Composable
private fun OrganizationList(organizations: List<Organization>, onSelect: (Long) -> Unit, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.organization_picker_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(organizations, key = { it.id }) { organization ->
            Card(onClick = { onSelect(organization.id) }, enabled = organization.isActive, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(organization.name, style = MaterialTheme.typography.titleMedium)
                    organization.address?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!organization.isActive) {
                        Text(
                            stringResource(R.string.organization_inactive),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/** Signed in but not a member of anything yet, the normal state right after registering. */
@Composable
private fun NoOrganization(onRetry: () -> Unit, onSignOut: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.organization_none_title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.organization_none_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.organization_check_again)) }
        OutlinedButton(onClick = onSignOut) { Text(stringResource(R.string.action_sign_out)) }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizationPickerPreview() {
    EchnoTheme {
        OrganizationPickerContent(
            uiState =
            OrganizationPickerUiState.Loaded(
                listOf(
                    Organization(1, "Malabar Builders", "Kozhikode, Kerala", isActive = true),
                    Organization(2, "Periyar Infra", null, isActive = false)
                )
            ),
            onSelect = {},
            onRetry = {},
            onSignOut = {}
        )
    }
}

@Preview(showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoOrganizationPreview() {
    EchnoTheme {
        OrganizationPickerContent(OrganizationPickerUiState.Loaded(emptyList()), onSelect = {}, onRetry = {}, onSignOut = {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizationPickerErrorPreview() {
    EchnoTheme {
        OrganizationPickerContent(OrganizationPickerUiState.Error(AppError.Network), onSelect = {}, onRetry = {}, onSignOut = {})
    }
}
