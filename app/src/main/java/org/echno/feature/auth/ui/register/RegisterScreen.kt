package org.echno.feature.auth.ui.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import org.echno.core.designsystem.theme.EchnoTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onFormChange: (RegistrationForm) -> Unit,
    onSubmit: () -> Unit,
    onSubmitErrorShown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val submitErrorMessage = uiState.submitError?.let { stringResource(it.messageRes()) }
    LaunchedEffect(submitErrorMessage) {
        if (submitErrorMessage != null) {
            snackbarHostState.showSnackbar(submitErrorMessage)
            onSubmitErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            RegisterForm(uiState, onFormChange, onSubmit, onBack)
        }
    }
}

@Composable
private fun RegisterForm(uiState: RegisterUiState, onFormChange: (RegistrationForm) -> Unit, onSubmit: () -> Unit, onSignIn: () -> Unit) {
    val form = uiState.form
    val errors = uiState.errors
    val enabled = !uiState.isSubmitting

    Column(
        modifier =
        Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            // Keeps the focused field above the keyboard.
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(R.string.register_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FormTextField(
            value = form.fullName,
            onValueChange = { onFormChange(form.copy(fullName = it)) },
            label = R.string.register_full_name,
            error = errors[RegistrationField.FullName],
            enabled = enabled,
            contentType = ContentType.PersonFullName,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )
        FormTextField(
            value = form.username,
            onValueChange = { onFormChange(form.copy(username = it)) },
            label = R.string.register_username,
            error = errors[RegistrationField.Username],
            enabled = enabled,
            hint = R.string.register_username_hint,
            contentType = ContentType.NewUsername,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false, imeAction = ImeAction.Next)
        )
        FormTextField(
            value = form.email,
            onValueChange = { onFormChange(form.copy(email = it)) },
            label = R.string.register_email,
            error = errors[RegistrationField.Email],
            enabled = enabled,
            contentType = ContentType.EmailAddress,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
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
        DateOfBirthField(form.dateOfBirth, errors[RegistrationField.DateOfBirth], enabled) { onFormChange(form.copy(dateOfBirth = it)) }
        GenderField(form.gender, errors[RegistrationField.Gender], enabled) { onFormChange(form.copy(gender = it)) }
        RoleField(form.role, errors[RegistrationField.Role], enabled) { onFormChange(form.copy(role = it)) }
        PasswordField(
            value = form.password,
            onValueChange = { onFormChange(form.copy(password = it)) },
            label = R.string.register_password,
            error = errors[RegistrationField.Password],
            enabled = enabled,
            hint = R.string.register_password_hint,
            imeAction = ImeAction.Next
        )
        PasswordField(
            value = form.confirmPassword,
            onValueChange = { onFormChange(form.copy(confirmPassword = it)) },
            label = R.string.register_confirm_password,
            error = errors[RegistrationField.ConfirmPassword],
            enabled = enabled,
            imeAction = ImeAction.Done
        )
        TermsCheckbox(form.acceptedTerms, errors[RegistrationField.Terms], enabled) { onFormChange(form.copy(acceptedTerms = it)) }
        Button(onClick = onSubmit, enabled = enabled, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.action_create_account))
            }
        }
        TextButton(onClick = onSignIn, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.register_have_account))
        }
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = supportingText(error, hint),
        enabled = enabled,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        // Tells password managers and autofill what goes here.
        modifier = Modifier.fillMaxWidth().semantics { this.contentType = contentType }
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = supportingText(error, hint),
        enabled = enabled,
        singleLine = true,
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
        modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.NewPassword }
    )
}

/** Opens a calendar; typing a date isn't needed. Dates are handled in UTC, which is how DatePicker reports them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(date: LocalDate?, error: FieldError?, enabled: Boolean, onDateChange: (LocalDate) -> Unit) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatted = remember(date) { date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)).orEmpty() }

    Box {
        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.register_date_of_birth)) },
            trailingIcon = { Icon(painterResource(R.drawable.ic_calendar), contentDescription = null) },
            isError = error != null,
            supportingText = supportingText(error, null),
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.register_gender), style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Gender.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = gender == option,
                    onClick = { onGenderChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, Gender.entries.size),
                    enabled = enabled
                ) { Text(stringResource(option.labelRes())) }
            }
        }
        ErrorLine(error)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleField(role: AccountRole?, error: FieldError?, enabled: Boolean, onRoleChange: (AccountRole) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = role?.let { stringResource(it.labelRes()) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.register_role)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = supportingText(error, null),
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AccountRole.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
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
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
        ) {
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            Text(stringResource(R.string.register_terms), style = MaterialTheme.typography.bodyMedium)
        }
        ErrorLine(error)
    }
}

@Composable
private fun ErrorLine(error: FieldError?) {
    if (error != null) {
        Text(
            error.message(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/** The error if there is one, otherwise the hint, otherwise nothing. */
private fun supportingText(error: FieldError?, hint: Int?): (@Composable () -> Unit)? = when {
    error != null -> {
        { Text(error.message()) }
    }
    hint != null -> {
        { Text(stringResource(hint)) }
    }
    else -> null
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

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
        RegisterContent(
            uiState =
            RegisterUiState(
                form = RegistrationForm(fullName = "Asha", username = "a", email = "asha@site"),
                errors =
                mapOf(
                    RegistrationField.Username to FieldError.UsernameFormat,
                    RegistrationField.Email to FieldError.EmailTaken,
                    RegistrationField.Gender to FieldError.Required,
                    RegistrationField.Terms to FieldError.TermsNotAccepted
                )
            ),
            onFormChange = {},
            onSubmit = {},
            onSubmitErrorShown = {},
            onBack = {}
        )
    }
}
