package org.echno.feature.organization.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.echno.R
import org.echno.core.designsystem.component.EmptyState
import org.echno.core.designsystem.component.ErrorState
import org.echno.core.designsystem.component.FilledButton
import org.echno.core.designsystem.component.HeaderPillButton
import org.echno.core.designsystem.component.HorizonHeader
import org.echno.core.designsystem.component.LoadingState
import org.echno.core.designsystem.component.OutlineButton
import org.echno.core.designsystem.component.SiteTag
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.TabletPreviews
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

/**
 * The same layout on phones and tablets: organizations are cards in a grid that is one column
 * wide on a phone and fills with more columns as the window widens.
 */
@Composable
fun OrganizationPickerContent(
    uiState: OrganizationPickerUiState,
    onSelect: (Long) -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            HorizonHeader {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.organization_picker_title),
                        modifier = Modifier.weight(1f).semantics { heading() },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    HeaderPillButton(stringResource(R.string.action_sign_out), painterResource(R.drawable.ic_sign_out), onClick = onSignOut)
                }
            }
            Box(Modifier.weight(1f)) {
                when (uiState) {
                    OrganizationPickerUiState.Loading -> LoadingState()
                    is OrganizationPickerUiState.Error ->
                        ErrorState(
                            title = stringResource(R.string.organization_error_title),
                            message = stringResource(uiState.error.messageRes()),
                            onRetry = onRetry
                        )
                    is OrganizationPickerUiState.Loaded ->
                        if (uiState.organizations.isEmpty()) {
                            NoOrganization(onRetry, onSignOut)
                        } else {
                            OrganizationGrid(uiState.organizations, onSelect)
                        }
                }
            }
        }
    }
}

@Composable
private fun OrganizationGrid(organizations: List<Organization>, onSelect: (Long) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.widthIn(max = 1040.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(R.string.organization_picker_message),
                    modifier = Modifier.padding(bottom = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(organizations, key = { it.id }) { organization ->
                OrganizationCard(organization, onClick = { onSelect(organization.id) })
            }
        }
    }
}

/** An organization to work in. An inactive one can't be chosen: the card is dimmed and tagged. */
@Composable
private fun OrganizationCard(organization: Organization, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = organization.isActive,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier =
            Modifier
                .heightIn(min = 76.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .alpha(if (organization.isActive) 1f else 0.7f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_organization), contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(organization.name, style = MaterialTheme.typography.titleSmall)
                organization.address?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!organization.isActive) SiteTag(stringResource(R.string.organization_inactive))
        }
    }
}

/** Signed in but not a member of anything yet, the normal state right after registering. */
@Composable
private fun NoOrganization(onRetry: () -> Unit, onSignOut: () -> Unit) {
    EmptyState(
        title = stringResource(R.string.organization_none_title),
        message = stringResource(R.string.organization_none_message),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledButton(
                stringResource(R.string.organization_check_again),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp
            )
            OutlineButton(stringResource(R.string.action_sign_out), onClick = onSignOut, modifier = Modifier.fillMaxWidth(), height = 52.dp)
        }
    }
}

private val previewOrganizations =
    listOf(
        Organization(1, "Malabar Builders", "Kozhikode, Kerala", isActive = true),
        Organization(3, "Tornotron Builders", "MG Road, Kochi", isActive = true),
        Organization(2, "Periyar Infra", null, isActive = false)
    )

@Preview(showSystemUi = true)
@Composable
private fun OrganizationPickerPreview() {
    EchnoTheme {
        OrganizationPickerContent(OrganizationPickerUiState.Loaded(previewOrganizations), onSelect = {}, onRetry = {}, onSignOut = {})
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

@TabletPreviews
@Composable
private fun OrganizationPickerTabletPreview() {
    EchnoTheme {
        OrganizationPickerContent(OrganizationPickerUiState.Loaded(previewOrganizations), onSelect = {}, onRetry = {}, onSignOut = {})
    }
}
