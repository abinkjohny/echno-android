package org.echno.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.echno.R
import org.echno.core.designsystem.component.ErrorState
import org.echno.core.designsystem.component.LoadingState
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.ui.messageRes
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.ui.register.labelRes
import org.echno.feature.organization.domain.Organization
import org.echno.feature.profile.domain.Profile

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(uiState = uiState, onRetry = viewModel::retry, modifier = modifier, topBarActions = topBarActions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.profile_title)) }, actions = topBarActions) }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            ProfileUiState.Loading -> LoadingState(contentModifier)
            is ProfileUiState.Error ->
                ErrorState(message = stringResource(uiState.error.messageRes()), onRetry = onRetry, modifier = contentModifier)
            is ProfileUiState.Loaded -> ProfileDetails(uiState.profile, contentModifier)
        }
    }
}

@Composable
private fun ProfileDetails(profile: Profile, modifier: Modifier = Modifier) {
    // Localized: the device's language and region decide the order and month names.
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(profile)

        Section(
            title = stringResource(R.string.profile_section_contact),
            rows =
            listOf(
                stringResource(R.string.profile_label_email) to profile.email,
                stringResource(R.string.profile_label_phone) to profile.phone,
                stringResource(R.string.profile_label_emergency_contact) to profile.emergencyContact,
                stringResource(R.string.profile_label_address) to profile.address
            )
        )
        Section(
            title = stringResource(R.string.profile_section_personal),
            rows =
            listOf(
                stringResource(R.string.profile_label_gender) to profile.gender?.let { stringResource(it.labelRes()) },
                stringResource(R.string.profile_label_date_of_birth) to profile.dateOfBirth?.format(dateFormat),
                stringResource(R.string.profile_label_blood_group) to profile.bloodGroup
            )
        )
        Section(
            title = stringResource(R.string.profile_section_work),
            rows =
            listOf(
                stringResource(R.string.profile_label_qualification) to profile.qualification,
                stringResource(R.string.profile_label_experience) to
                    profile.experienceYears?.let { pluralStringResource(R.plurals.profile_experience_years, it, it) }
            ),
            tagGroups =
            listOf(
                stringResource(R.string.profile_label_skills) to profile.skills,
                stringResource(R.string.profile_label_certifications) to profile.certifications
            )
        )

        profile.memberSince?.let {
            Text(
                stringResource(R.string.profile_member_since, it.format(dateFormat)),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Initials, name, role and the organization the user works in now. */
@Composable
private fun ProfileHeader(profile: Profile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            // The name is read out right below, so the initials add nothing for TalkBack.
            modifier = Modifier.size(88.dp).clearAndSetSemantics {},
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(initials(profile.name), style = MaterialTheme.typography.headlineMedium)
            }
        }
        Text(
            profile.name,
            modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        profile.role?.let {
            Text(stringResource(it.labelRes()), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        }
        profile.organization?.let {
            Text(it.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** A card of label/value rows. Rows without a value are left out, and so is a section with nothing to show. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Section(title: String, rows: List<Pair<String, String?>>, tagGroups: List<Pair<String, List<String>>> = emptyList()) {
    val filled = rows.mapNotNull { (label, value) -> value?.let { label to it } }
    val tags = tagGroups.filter { (_, values) -> values.isNotEmpty() }
    if (filled.isEmpty() && tags.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
            filled.forEach { (label, value) -> LabeledValue(label, value) }
            tags.forEach { (label, values) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Label(label)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        values.forEach { Tag(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Label(label)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Tag(text: String) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
    }
}

/** "Asha Menon" → "AM", "asha@example.com" → "A". */
internal fun initials(name: String): String = name.split(' ', '.', '_', '-', '@')
    .filter { it.isNotBlank() }
    .let { words -> if ('@' in name) words.take(1) else words.take(2) }
    .joinToString("") { it.first().uppercase() }
    .ifEmpty { "?" }

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    EchnoTheme {
        ProfileContent(
            uiState =
            ProfileUiState.Loaded(
                Profile(
                    id = 42,
                    name = "Asha Menon",
                    email = "asha@example.com",
                    phone = "+919876543210",
                    role = AccountRole.HrManager,
                    gender = Gender.Female,
                    dateOfBirth = LocalDate.of(1994, 5, 1),
                    bloodGroup = "O+",
                    address = "12 Lake Road, Kochi",
                    emergencyContact = null,
                    qualification = "MBA",
                    experienceYears = 6,
                    skills = listOf("Payroll", "Recruitment", "Labour law"),
                    certifications = emptyList(),
                    memberSince = LocalDate.of(2026, 1, 15),
                    organization = Organization(5, "Malabar Builders", "Kozhikode", isActive = true)
                )
            ),
            onRetry = {}
        )
    }
}
