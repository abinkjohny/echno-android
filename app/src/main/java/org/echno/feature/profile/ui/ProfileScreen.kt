package org.echno.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.echno.R
import org.echno.core.designsystem.component.DetailCard
import org.echno.core.designsystem.component.DetailRow
import org.echno.core.designsystem.component.ErrorState
import org.echno.core.designsystem.component.FilledButton
import org.echno.core.designsystem.component.Horizon
import org.echno.core.designsystem.component.HorizonHeader
import org.echno.core.designsystem.component.LabeledValue
import org.echno.core.designsystem.component.LoadingState
import org.echno.core.designsystem.component.OutlineButton
import org.echno.core.designsystem.component.RowActionButton
import org.echno.core.designsystem.component.SectionHeader
import org.echno.core.designsystem.component.SiteTag
import org.echno.core.designsystem.theme.Bricolage
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.HeaderChipShape
import org.echno.core.ui.AccountActions
import org.echno.core.ui.AccountMenu
import org.echno.core.ui.ContactLauncher
import org.echno.core.ui.LayoutWidth
import org.echno.core.ui.TabletPreviews
import org.echno.core.ui.currentLayoutWidth
import org.echno.core.ui.messageRes
import org.echno.core.ui.rememberContactLauncher
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.ui.register.labelRes
import org.echno.feature.organization.domain.Organization
import org.echno.feature.profile.domain.Profile

@Composable
fun ProfileScreen(account: AccountActions, modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(uiState = uiState, onRetry = viewModel::retry, account = account, modifier = modifier)
}

/**
 * Phones (and tablets held upright): the horizon header with who you are, then the numbered
 * sections. Tablets on their side: who you are and the account actions in a navy panel on the
 * left, the sections in two columns on the right.
 */
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    account: AccountActions,
    modifier: Modifier = Modifier,
    layoutWidth: LayoutWidth = currentLayoutWidth(),
    contacts: ContactLauncher = rememberContactLauncher()
) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (uiState is ProfileUiState.Loaded && layoutWidth == LayoutWidth.Expanded) {
            WideProfile(uiState.profile, account, contacts)
        } else {
            NarrowProfile(uiState, onRetry, account, contacts)
        }
    }
}

@Composable
private fun NarrowProfile(uiState: ProfileUiState, onRetry: () -> Unit, account: AccountActions, contacts: ContactLauncher) {
    val profile = (uiState as? ProfileUiState.Loaded)?.profile
    Column(Modifier.fillMaxSize().then(if (profile != null) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
        HorizonHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.profile_title),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleLarge
                )
                AccountMenu(account)
            }
            if (profile != null) {
                Identity(profile, avatarSize = 72.dp, horizontal = true)
                profile.organization?.let { OrganizationChip(it, account) }
            }
        }
        when (uiState) {
            ProfileUiState.Loading -> LoadingState(Modifier.weight(1f))
            is ProfileUiState.Error ->
                ErrorState(
                    title = stringResource(R.string.profile_error_title),
                    message = stringResource(uiState.error.messageRes()),
                    onRetry = onRetry,
                    modifier = Modifier.weight(1f)
                )
            is ProfileUiState.Loaded ->
                Column(
                    modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 640.dp)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val sections = sectionsFor(uiState.profile, contacts)
                    sections.forEach { it() }
                    uiState.profile.memberSince?.let {
                        Text(
                            stringResource(R.string.profile_member_since, it.format(dateFormat())),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
        }
    }
}

@Composable
private fun WideProfile(profile: Profile, account: AccountActions, contacts: ContactLauncher) {
    val colors = EchnoTheme.colors
    Row(Modifier.fillMaxSize()) {
        // Who you are, and what you can do with the account, in the navy panel.
        Column(Modifier.width(360.dp).fillMaxHeight().background(colors.header)) {
            Column(
                modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(start = 32.dp, top = 40.dp, end = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides colors.onHeader) {
                    Identity(profile, avatarSize = 104.dp, horizontal = false)
                    profile.organization?.let { OrganizationChip(it, account) }
                    profile.memberSince?.let {
                        Text(
                            stringResource(R.string.profile_member_since, it.format(dateFormat())),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onHeaderMuted
                        )
                    }
                }
            }
            Horizon(sun = true, height = 96.dp, ground = colors.headerChip, sunRadius = 34.dp, sunCenterY = 0.65f)
            Column(
                modifier = Modifier.fillMaxWidth().background(
                    colors.headerChip
                ).navigationBarsPadding().padding(start = 32.dp, top = 4.dp, end = 32.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (account.canSwitchOrganization) {
                    OutlineButton(
                        stringResource(R.string.action_switch_organization),
                        onClick = account.onSwitchOrganization,
                        modifier = Modifier.fillMaxWidth(),
                        height = 52.dp,
                        contentColor = colors.onHeader,
                        borderColor = colors.onHeaderMuted,
                        icon = painterResource(R.drawable.ic_organization)
                    )
                }
                FilledButton(
                    stringResource(R.string.action_sign_out),
                    onClick = account.onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    height = 52.dp,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    icon = painterResource(R.drawable.ic_sign_out)
                )
            }
        }
        // The sections, in two columns: Contact on the left, the rest on the right.
        val sections = sectionsFor(profile, contacts)
        Row(
            modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(36.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) { sections.take(1).forEach { it() } }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) { sections.drop(1).forEach { it() } }
        }
    }
}

/** Initials, name and role. Sits on the navy header, so it uses the header's content colour. */
@Composable
private fun Identity(profile: Profile, avatarSize: Dp, horizontal: Boolean) {
    val avatar: @Composable () -> Unit = {
        Surface(
            // The name is read out right beside it, so the initials add nothing for TalkBack.
            modifier = Modifier.size(avatarSize).clearAndSetSemantics {},
            shape = CircleShape,
            color = EchnoTheme.colors.avatar,
            contentColor = EchnoTheme.colors.onAvatar
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    initials(profile.name),
                    fontFamily = Bricolage,
                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight,
                    fontSize = (
                        avatarSize.value *
                            0.39f
                        ).sp
                )
            }
        }
    }
    val text: @Composable ColumnScope.() -> Unit = {
        Text(
            profile.name,
            modifier = Modifier.semantics { heading() },
            style = if (horizontal) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge
        )
        profile.role?.let {
            Text(stringResource(it.labelRes()), style = MaterialTheme.typography.bodyLarge, color = EchnoTheme.colors.onHeaderMuted)
        }
    }
    if (horizontal) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            avatar()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), content = text)
        }
    } else {
        avatar()
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = text)
    }
}

/** The organization you're working in. Tapping it switches organization, when you belong to more than one. */
@Composable
private fun OrganizationChip(organization: Organization, account: AccountActions) {
    val colors = EchnoTheme.colors
    val switchLabel = stringResource(R.string.action_switch_organization)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.heightIn(min = 40.dp).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_organization), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(organization.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
    // TalkBack says "double-tap to Switch Organization" rather than a bare "button".
    val clickable =
        if (account.canSwitchOrganization) {
            Modifier.clip(HeaderChipShape).clickable(onClickLabel = switchLabel, role = Role.Button, onClick = account.onSwitchOrganization)
        } else {
            Modifier
        }
    Surface(modifier = clickable, shape = HeaderChipShape, color = colors.headerChip, contentColor = colors.onHeader, content = content)
}

/**
 * The numbered sections that have something to show: Contact, Work, Personal. Rows without a value
 * are left out, and so is a section with nothing in it; numbering follows what's shown.
 */
@Composable
private fun sectionsFor(profile: Profile, contacts: ContactLauncher): List<@Composable () -> Unit> {
    val call = stringResource(R.string.profile_action_call)
    val email = stringResource(R.string.profile_action_email)
    val contact =
        listOfNotNull(
            profile.email?.let { value ->
                ContactRow(
                    stringResource(R.string.profile_label_email),
                    value,
                    email,
                    stringResource(R.string.profile_email_description, value),
                    contacts.canEmail
                ) {
                    contacts.email(value)
                }
            },
            profile.phone?.let { value ->
                ContactRow(
                    stringResource(R.string.profile_label_phone),
                    value,
                    call,
                    stringResource(R.string.profile_call_description, value),
                    contacts.canDial
                ) {
                    contacts.dial(value)
                }
            },
            profile.emergencyContact?.let { value ->
                ContactRow(
                    stringResource(R.string.profile_label_emergency_contact),
                    value,
                    call,
                    stringResource(R.string.profile_call_description, value),
                    contacts.canDial
                ) { contacts.dial(value) }
            },
            profile.address?.let { ContactRow(stringResource(R.string.profile_label_address), it) }
        )
    val work =
        listOfNotNull(
            profile.qualification?.let { stringResource(R.string.profile_label_qualification) to it },
            profile.experienceYears?.let {
                stringResource(R.string.profile_label_experience) to pluralStringResource(R.plurals.profile_experience_years, it, it)
            }
        )
    val tags =
        listOf(
            stringResource(R.string.profile_label_skills) to profile.skills,
            stringResource(R.string.profile_label_certifications) to profile.certifications
        ).filter { (_, values) -> values.isNotEmpty() }
    val personal =
        listOfNotNull(
            profile.gender?.let { stringResource(R.string.profile_label_gender) to stringResource(it.labelRes()) },
            profile.dateOfBirth?.let { stringResource(R.string.profile_label_date_of_birth) to it.format(dateFormat()) },
            profile.bloodGroup?.let { stringResource(R.string.profile_label_blood_group) to it }
        )

    val titles = mutableListOf<Pair<String, @Composable () -> Unit>>()
    if (contact.isNotEmpty()) titles += stringResource(R.string.profile_section_contact) to { ContactCard(contact) }
    if (work.isNotEmpty() || tags.isNotEmpty()) titles += stringResource(R.string.profile_section_work) to { WorkCard(work, tags) }
    if (personal.isNotEmpty()) titles += stringResource(R.string.profile_section_personal) to { GridCard(personal, columns = 3) }
    return titles.mapIndexed { index, (title, card) ->
        {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(index + 1, title)
                card()
            }
        }
    }
}

private class ContactRow(
    val label: String,
    val value: String,
    val action: String? = null,
    val actionDescription: String? = null,
    val actionAvailable: Boolean = false,
    val onAction: () -> Unit = {}
)

@Composable
private fun ContactCard(rows: List<ContactRow>) {
    DetailCard {
        rows.forEachIndexed { index, row ->
            val action: (@Composable RowScope.() -> Unit)? =
                if (row.action != null && row.actionAvailable) {
                    { RowActionButton(row.action, row.actionDescription.orEmpty(), row.onAction) }
                } else {
                    null
                }
            DetailRow(row.label, row.value, showRule = index < rows.lastIndex, action = action)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkCard(values: List<Pair<String, String>>, tagGroups: List<Pair<String, List<String>>>) {
    DetailCard {
        if (values.isNotEmpty()) {
            ValueGrid(values, columns = 2)
            if (tagGroups.isNotEmpty()) HorizontalDivider(color = EchnoTheme.colors.cardRule)
        }
        tagGroups.forEach { (label, tags) ->
            Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { SiteTag(it) }
                }
            }
        }
    }
}

@Composable
private fun GridCard(values: List<Pair<String, String>>, columns: Int) {
    DetailCard { ValueGrid(values, columns) }
}

/** Short values side by side, [columns] to a row; at large text sizes they still wrap inside their cell. */
@Composable
private fun ValueGrid(values: List<Pair<String, String>>, columns: Int) {
    Column {
        values.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) -> LabeledValue(label, value, Modifier.weight(1f).padding(vertical = 14.dp)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// Localized: the device's language and region decide the order and month names.
private fun dateFormat(): DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** "Asha Menon" → "AM", "asha@example.com" → "A". */
internal fun initials(name: String): String = name.split(' ', '.', '_', '-', '@')
    .filter { it.isNotBlank() }
    .let { words -> if ('@' in name) words.take(1) else words.take(2) }
    .joinToString("") { it.first().uppercase() }
    .ifEmpty { "?" }

private val previewProfile =
    Profile(
        id = 42,
        name = "Asha Menon",
        email = "asha.menon@tornotron.in",
        phone = "+91 98470 12345",
        role = AccountRole.HrManager,
        gender = Gender.Female,
        dateOfBirth = LocalDate.of(1990, 5, 12),
        bloodGroup = "B+",
        address = "MG Road, Kochi, Kerala",
        emergencyContact = "+91 94460 67890",
        qualification = "MBA, HR",
        experienceYears = 8,
        skills = listOf("Payroll", "Site safety", "Labour law"),
        certifications = emptyList(),
        memberSince = LocalDate.of(2024, 3, 15),
        organization = Organization(5, "Tornotron Builders", "Kochi", isActive = true)
    )

@Preview(name = "Light", showSystemUi = true)
@Preview(name = "Dark", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfilePreview() {
    EchnoTheme {
        ProfileContent(uiState = ProfileUiState.Loaded(previewProfile), onRetry = {}, account = AccountActions.None)
    }
}

@TabletPreviews
@Composable
private fun ProfileTabletPreview() {
    EchnoTheme {
        ProfileContent(
            uiState = ProfileUiState.Loaded(previewProfile),
            onRetry = {},
            account = AccountActions.None.copy(canSwitchOrganization = true),
            layoutWidth = LayoutWidth.Expanded
        )
    }
}
