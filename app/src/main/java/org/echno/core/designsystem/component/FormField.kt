package org.echno.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme

/**
 * A text field with its label above it rather than inside, so the label never shrinks or
 * disappears while typing. Below it, the [error] if there is one, otherwise the [hint].
 * [fieldModifier] goes on the input itself (autofill content types, dropdown anchors).
 */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    error: String? = null,
    hint: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // TalkBack reads the label and any error with the field itself, so the label text is hidden from it.
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clearAndSetSemantics { })
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
            fieldModifier.fillMaxWidth().semantics {
                contentDescription = label
                if (error != null) error(error)
            },
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            isError = error != null,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            shape = MaterialTheme.shapes.small,
            colors = formFieldColors()
        )
        FieldMessage(error, hint)
    }
}

/** The error (in the error colour) or the hint under a field; nothing if neither is set. */
@Composable
fun FieldMessage(error: String?, hint: String? = null) {
    val text = error ?: hint ?: return
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedBorderColor = MaterialTheme.colorScheme.primary
)

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun FormFieldPreview() {
    EchnoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FormField(value = "Asha Menon", onValueChange = {}, label = "Full Name")
            FormField(value = "a", onValueChange = {}, label = "Username", error = "Use 4–20 letters, numbers, dots, dashes or underscores")
        }
    }
}
