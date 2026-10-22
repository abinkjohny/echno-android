package org.echno.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme

// Buttons are 56dp tall (60dp on tablets, via `height`) with a word on every one.
// - PrimaryButton: amber, the one main action on a screen ("Sign In", "Create Account").
// - FilledButton: navy, a strong action that isn't the main one ("Try Again").
// - OutlineButton: navy outline, the alternative ("Create Account" beside "Sign In").

val ButtonHeight = 56.dp
val TabletButtonHeight = 60.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = ButtonHeight
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = height),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.medium,
        colors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        contentPadding = ContentPadding
    ) { ButtonContent(text, loading) }
}

@Composable
fun FilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = ButtonHeight,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: Painter? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = height),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = ContentPadding
    ) { ButtonContent(text, loading, icon) }
}

/** [contentColor] also colours the outline, so the button can sit on the navy header as well as on paper. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = ButtonHeight,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = contentColor,
    icon: Painter? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = height),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.5.dp, if (enabled) borderColor else borderColor.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        contentPadding = ContentPadding
    ) { ButtonContent(text, loading = false, icon) }
}

private val ContentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)

@Composable
private fun ButtonContent(text: String, loading: Boolean, icon: Painter? = null) {
    if (loading) {
        // The spinner replaces the label on screen while the request runs; TalkBack still reads the label.
        CircularProgressIndicator(
            Modifier.size(22.dp).semantics { contentDescription = text },
            color = LocalContentColor.current,
            strokeWidth = 2.5.dp
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonsPreview() {
    EchnoTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Sign In", onClick = {})
            FilledButton("Try Again", onClick = {})
            OutlineButton("Create Account", onClick = {})
            PrimaryButton("Sign In", onClick = {}, loading = true)
        }
    }
}
