package org.echno.feature.organization.ui.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.echno.R
import org.echno.core.designsystem.component.ButtonHeight
import org.echno.core.designsystem.component.CodeInput
import org.echno.core.designsystem.component.PageHeader
import org.echno.core.designsystem.component.PrimaryButton
import org.echno.core.designsystem.component.TabletButtonHeight
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.LayoutWidth
import org.echno.core.ui.TabletPreviews
import org.echno.core.ui.currentLayoutWidth
import org.echno.core.ui.messageRes
import org.echno.feature.organization.domain.Organization

@Composable
fun JoinOrganizationScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: JoinOrganizationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    JoinOrganizationContent(
        uiState = uiState,
        onCodeChange = viewModel::onCodeChange,
        onJoin = viewModel::join,
        onBack = onBack,
        modifier = modifier
    )
}

/**
 * A focused page: the invitation code in five boxes and one Join button. The same column on
 * phones and tablets, centred, since there's only one thing to do here.
 */
@Composable
fun JoinOrganizationContent(
    uiState: JoinOrganizationUiState,
    onCodeChange: (String) -> Unit,
    onJoin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    layoutWidth: LayoutWidth = currentLayoutWidth()
) {
    val wide = layoutWidth == LayoutWidth.Expanded
    val focusRequester = remember { FocusRequester() }
    // Opens the keyboard on arrival: typing the code is the only thing to do here.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            PageHeader(
                title = stringResource(R.string.join_title),
                backLabel = stringResource(if (wide) R.string.action_cancel else R.string.action_back),
                backIcon = painterResource(if (wide) R.drawable.ic_close else R.drawable.ic_chevron_back),
                onBack = onBack,
                compact = !wide
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier =
                    Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = if (wide) 32.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(stringResource(R.string.join_intro), style = MaterialTheme.typography.bodyLarge)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.join_code_label), style = MaterialTheme.typography.labelMedium)
                        CodeInput(
                            value = uiState.code,
                            onValueChange = onCodeChange,
                            length = INVITE_CODE_LENGTH,
                            label = stringResource(R.string.join_code_label),
                            // Once joined the code was right, so the boxes don't show an error even while offline.
                            error = uiState.error?.takeIf { uiState.joined == null }?.let { joinErrorMessage(it, null) },
                            enabled = !uiState.isJoining && uiState.joined == null,
                            focusRequester = focusRequester,
                            onDone = onJoin
                        )
                    }
                    Message(uiState)
                    PrimaryButton(
                        stringResource(if (uiState.joined != null) R.string.action_continue else R.string.action_join),
                        onClick = onJoin,
                        enabled = uiState.canSubmit,
                        loading = uiState.isJoining,
                        height = if (wide) TabletButtonHeight else ButtonHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** The error if there is one, otherwise where the code comes from. Announced when it changes. */
@Composable
private fun Message(uiState: JoinOrganizationUiState) {
    val error = uiState.error
    Text(
        text = if (error != null) joinErrorMessage(error, uiState.joined) else stringResource(R.string.join_hint),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = if (error != null && uiState.joined == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** What went wrong, in terms of the code: the shared sentences talk about records, not codes. */
@Composable
private fun joinErrorMessage(error: AppError, joined: Organization?): String = when {
    joined != null -> stringResource(R.string.join_joined_offline, joined.name)
    error is AppError.NotFound -> stringResource(R.string.join_error_not_found)
    error is AppError.Validation -> stringResource(R.string.join_error_invalid)
    error is AppError.RateLimited && error.retryAfterSeconds != null -> {
        // Whole minutes, rounded up, so "in 1 minute" is never too early.
        val minutes = ((error.retryAfterSeconds + 59) / 60).toInt().coerceAtLeast(1)
        pluralStringResource(R.plurals.join_error_rate_limited, minutes, minutes)
    }
    else -> stringResource(error.messageRes())
}

@Preview(showSystemUi = true)
@Composable
private fun JoinOrganizationPreview() {
    EchnoTheme {
        JoinOrganizationContent(JoinOrganizationUiState(code = "048"), onCodeChange = {}, onJoin = {}, onBack = {})
    }
}

@Preview(showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun JoinOrganizationErrorPreview() {
    EchnoTheme {
        JoinOrganizationContent(
            JoinOrganizationUiState(code = "11111", error = AppError.NotFound(null)),
            onCodeChange = {},
            onJoin = {},
            onBack = {}
        )
    }
}

@TabletPreviews
@Composable
private fun JoinOrganizationTabletPreview() {
    EchnoTheme {
        JoinOrganizationContent(
            JoinOrganizationUiState(code = "04821"),
            onCodeChange = {},
            onJoin = {},
            onBack = {},
            layoutWidth = LayoutWidth.Expanded
        )
    }
}
