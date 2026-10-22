package org.echno.feature.auth.ui.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.echno.R
import org.echno.core.designsystem.component.ButtonHeight
import org.echno.core.designsystem.component.ChoiceTrack
import org.echno.core.designsystem.component.FieldMessage
import org.echno.core.designsystem.component.FormField
import org.echno.core.designsystem.component.PageHeader
import org.echno.core.designsystem.component.PrimaryButton
import org.echno.core.designsystem.component.TabletButtonHeight
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.network.AppError
import org.echno.core.ui.LayoutWidth
import org.echno.core.ui.TabletPreviews
import org.echno.core.ui.currentLayoutWidth
import org.echno.core.ui.messageRes
import org.echno.feature.auth.domain.AccountRole
import org.echno.feature.auth.domain.FieldError
import org.echno.feature.auth.domain.Gender
import org.echno.feature.auth.domain.RegistrationField
import org.echno.feature.auth.domain.RegistrationForm

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: (username: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentOnRegistered by rememberUpdatedState(onRegistered)
    LaunchedEffect(uiState.registeredUsername) {
        uiState.registeredUsername?.let { currentOnRegistered(it) }
    }

    RegisterContent(
        uiState = uiState,
        onFormChange = viewModel::onFormChange,
        onSubmit = viewModel::submit,
        onSubmitErrorShown = viewModel::onSubmitErrorShown,
        onBack = onBack,
        modifier = modifier
    )
}

/**
 * A focused page. Phones: a back link in the header, one column of fields and the button at the
 * end. Tablets on their side: a Cancel pill in the header, fields in two columns, and the button
 * in a bar along the bottom that stays put while the fields scroll.
 */
@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onFormChange: (RegistrationForm) -> Unit,
    onSubmit: () -> Unit,
    onSubmitErrorShown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    layoutWidth: LayoutWidth = currentLayoutWidth()
) {
    val wide = layoutWidth == LayoutWidth.Expanded
    val snackbarHostState = remember { SnackbarHostState() }
    val submitErrorMessage =
        uiState.submitError?.let { error ->
            // The shared 409 sentence is about editing a record; here it means the details clash with an account.
            stringResource(if (error is AppError.Conflict) R.string.register_error_conflict else error.messageRes())
        }
    LaunchedEffect(submitErrorMessage) {
        if (submitErrorMessage != null) {
            snackbarHostState.showSnackbar(submitErrorMessage)
            onSubmitErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // The header pads itself below the status bar; the body handles the keyboard and navigation bar.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            PageHeader(
                title = stringResource(R.string.register_title),
                backLabel = stringResource(if (wide) R.string.action_cancel else R.string.action_back),
                backIcon = painterResource(if (wide) R.drawable.ic_close else R.drawable.ic_chevron_back),
                onBack = onBack,
                compact = !wide
            )
        },
        bottomBar = { if (wide) TabletActionBar(uiState, onSubmit, onSignIn = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            RegisterForm(uiState, onFormChange, onSubmit, onSignIn = onBack, wide = wide)
        }
    }
}

@Composable
private fun RegisterForm(
    uiState: RegisterUiState,
    onFormChange: (RegistrationForm) -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
    wide: Boolean
) {
    val form = uiState.form
    val errors = uiState.errors
    val enabled = !uiState.isSubmitting

    // Each field once, laid out below in one column or in pairs. The order is also the keyboard's Next order.
    val fields: List<@Composable () -> Unit> =
        listOf(
            {
                FormTextField(
                    value = form.fullName,
                    onValueChange = { onFormChange(form.copy(fullName = it)) },
                    label = R.string.register_full_name,
                    error = errors[RegistrationField.FullName],
                    enabled = enabled,
                    contentType = ContentType.PersonFullName,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
            },
            {
                FormTextField(
                    value = form.username,
                    onValueChange = { onFormChange(form.copy(username = it)) },
                    label = R.string.register_username,
                    error = errors[RegistrationField.Username],
                    enabled = enabled,
                    hint = R.string.register_username_hint,
                    contentType = ContentType.NewUsername,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    )
                )
            },
            {
                FormTextField(
                    value = form.email,
                    onValueChange = { onFormChange(form.copy(email = it)) },
                    label = R.string.register_email,
                    error = errors[RegistrationField.Email],
                    enabled = enabled,
                    contentType = ContentType.EmailAddress,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
            },
            {
                FormTextField(
                    value = form.phone,
                    onValueChange = { onFormChange(form.copy(phone = it)) },
                    label = R.string.register_phone,
                    error = errors[RegistrationField.Phone],
                    enabled = enabled,
                    hint = R.string.register_phone_hint,
                    contentType = ContentType.PhoneNumber,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
                )
            },
            {
                DateOfBirthField(form.dateOfBirth, errors[RegistrationField.DateOfBirth], enabled) {
                    onFormChange(form.copy(dateOfBirth = it))
                }
            },
            { GenderField(form.gender, errors[RegistrationField.Gender], enabled) { onFormChange(form.copy(gender = it)) } },
            {
                PasswordField(
                    value = form.password,
                    onValueChange = { onFormChange(form.copy(password = it)) },
                    label = R.string.register_password,
                    error = errors[RegistrationField.Password],
                    enabled = enabled,
                    hint = R.string.register_password_hint,
                    imeAction = ImeAction.Next
                )
            },
            {
                PasswordField(
                    value = form.confirmPassword,
                    onValueChange = { onFormChange(form.copy(confirmPassword = it)) },
                    label = R.string.register_confirm_password,
                    error = errors[RegistrationField.ConfirmPassword],
                    enabled = enabled,
                    imeAction = ImeAction.Done
                )
            },
            { RoleField(form.role, errors[RegistrationField.Role], enabled) { onFormChange(form.copy(role = it)) } }
        )

    Column(
        modifier =
        Modifier
            .widthIn(max = if (wide) 960.dp else 560.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            // Keeps the focused field above the keyboard.
            .imePadding()
            .then(if (wide) Modifier else Modifier.navigationBarsPadding())
            .padding(horizontal = if (wide) 40.dp else 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.register_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (wide) {
            fields.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    pair.forEach { field -> Box(Modifier.weight(1f)) { field() } }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            fields.forEach { it() }
        }
        TermsCheckbox(form.acceptedTerms, errors[RegistrationField.Terms], enabled) { onFormChange(form.copy(acceptedTerms = it)) }
        if (!wide) {
            PrimaryButton(
                stringResource(R.string.action_create_account),
                onClick = onSubmit,
                loading = uiState.isSubmitting,
                height = ButtonHeight,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            SignInLink(enabled, onSignIn, Modifier.fillMaxWidth())
        }
    }
}

/** Tablets: the way back to sign in on the left, Create Account on the right, above a hairline. */
@Composable
private fun TabletActionBar(uiState: RegisterUiState, onSubmit: () -> Unit, onSignIn: () -> Unit) {
    Column(Modifier.navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // As wide as the form above it, so the button lines up with the fields.
        Row(
            modifier = Modifier.widthIn(max = 960.dp).fillMaxWidth().padding(horizontal = 40.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SignInLink(!uiState.isSubmitting, onSignIn)
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                stringResource(R.string.action_create_account),
                onClick = onSubmit,
                loading = uiState.isSubmitting,
                height = TabletButtonHeight,
                modifier = Modifier.widthIn(min = 240.dp)
            )
        }
    }
}

@Composable
private fun SignInLink(enabled: Boolean, onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onSignIn, enabled = enabled, modifier = modifier.heightIn(min = 48.dp)) {
        Text(stringResource(R.string.register_have_account), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    error: FieldError?,
    enabled: Boolean,
    contentType: ContentType,
    keyboardOptions: KeyboardOptions,
    hint: Int? = null
) {
    FormField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(label),
        error = error?.message(),
        hint = hint?.let { stringResource(it) },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        // Tells password managers and autofill what goes here.
        fieldModifier = Modifier.semantics { this.contentType = contentType }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    error: FieldError?,
    enabled: Boolean,
    imeAction: ImeAction,
    hint: Int? = null
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    FormField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(label),
        error = error?.message(),
        hint = hint?.let { stringResource(it) },
        enabled = enabled,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = imeAction),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painterResource(if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                    contentDescription = stringResource(if (visible) R.string.register_hide_password else R.string.register_show_password)
                )
            }
        },
        fieldModifier = Modifier.semantics { contentType = ContentType.NewPassword }
    )
}

/** Opens a calendar; typing a date isn't needed. Dates are handled in UTC, which is how DatePicker reports them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(date: LocalDate?, error: FieldError?, enabled: Boolean, onDateChange: (LocalDate) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatted = remember(date) { date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)).orEmpty() }

    Box {
        FormField(
            value = formatted,
            onValueChange = {},
            label = stringResource(R.string.register_date_of_birth),
            readOnly = true,
            error = error?.message(),
            enabled = enabled,
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        )
        // A read-only text field doesn't react to taps, so a transparent layer on top opens the picker.
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = enabled, onClickLabel = stringResource(R.string.register_choose_date)) { showPicker = true }
        )
    }

    if (showPicker) {
        val today = remember { LocalDate.now() }
        val state =
            rememberDatePickerState(
                initialSelectedDateMillis = date?.toUtcMillis(),
                // Open on a plausible year instead of this month: nobody registering was born this year.
                initialDisplayedMonthMillis = (date ?: today.minusYears(25)).toUtcMillis(),
                yearRange = (today.year - 100)..today.year,
                selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today.toUtcMillis()
                }
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { onDateChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                        showPicker = false
                    }
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.action_cancel)) } }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun GenderField(gender: Gender?, error: FieldError?, enabled: Boolean, onGenderChange: (Gender) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.register_gender), style = MaterialTheme.typography.labelMedium)
        ChoiceTrack(
            options = Gender.entries,
            selected = gender,
            onSelect = onGenderChange,
            label = { stringResource(it.labelRes()) },
            enabled = enabled
        )
        FieldMessage(error?.message())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleField(role: AccountRole?, error: FieldError?, enabled: Boolean, onRoleChange: (AccountRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        FormField(
            value = role?.let { stringResource(it.labelRes()) }.orEmpty(),
            onValueChange = {},
            label = stringResource(R.string.register_role),
            readOnly = true,
            error = error?.message(),
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            fieldModifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AccountRole.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes()), style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onRoleChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TermsCheckbox(checked: Boolean, error: FieldError?, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column {
        // The whole row is one toggle, so the text is a big touch target and TalkBack reads it with the box.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                stringResource(R.string.register_terms),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        FieldMessage(error?.message())
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private val previewErrors =
    RegisterUiState(
        form = RegistrationForm(fullName = "Asha", username = "a", email = "asha@site"),
        errors =
        mapOf(
            RegistrationField.Username to FieldError.UsernameFormat,
            RegistrationField.Email to FieldError.EmailTaken,
            RegistrationField.Gender to FieldError.Required,
            RegistrationField.Terms to FieldError.TermsNotAccepted
        )
    )

@Preview(showSystemUi = true)
@Composable
private fun RegisterPreview() {
    EchnoTheme {
        RegisterContent(uiState = RegisterUiState(), onFormChange = {}, onSubmit = {}, onSubmitErrorShown = {}, onBack = {})
    }
}

@Preview(showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegisterErrorsPreview() {
    EchnoTheme {
        RegisterContent(uiState = previewErrors, onFormChange = {}, onSubmit = {}, onSubmitErrorShown = {}, onBack = {})
    }
}

@TabletPreviews
@Composable
private fun RegisterTabletPreview() {
    EchnoTheme {
        RegisterContent(
            uiState = previewErrors,
            onFormChange = {},
            onSubmit = {},
            onSubmitErrorShown = {},
            onBack = {},
            layoutWidth = LayoutWidth.Expanded
        )
    }
}
