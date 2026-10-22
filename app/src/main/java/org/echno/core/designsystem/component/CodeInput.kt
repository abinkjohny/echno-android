package org.echno.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.echno.core.designsystem.theme.EchnoTheme

/**
 * A short numeric code typed into a row of boxes, one digit each, like a one-time password.
 *
 * Underneath it is a single ordinary text field, hidden behind the boxes: typing, deleting,
 * pasting all five digits at once and TalkBack all work as in any field, and the boxes only
 * draw what it holds. Anything that isn't a digit is dropped, and so is anything past [length].
 * [onDone] runs from the keyboard's Done key.
 */
@Composable
fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onDone: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = { typed -> onValueChange(typed.filter(Char::isDigit).take(length)) },
        modifier =
        modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics {
                contentDescription = label
                if (error != null) error(error)
            },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box {
                // The real field: invisible, but still where the caret, selection and paste menu live.
                Box(Modifier.alpha(0f)) { innerTextField() }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(length) { index ->
                        // The box the next digit goes into; the last one once all are filled.
                        val active = focused && index == value.length.coerceAtMost(length - 1)
                        DigitBox(
                            digit = value.getOrNull(index),
                            active = active,
                            showCaret = active && value.length < length,
                            isError = error != null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun DigitBox(digit: Char?, active: Boolean, showCaret: Boolean, isError: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val border =
        when {
            isError -> BorderStroke(2.dp, colors.error)
            active -> BorderStroke(2.dp, colors.primary)
            else -> BorderStroke(1.dp, colors.outline)
        }
    Surface(
        modifier = modifier.heightIn(min = 64.dp),
        shape = MaterialTheme.shapes.small,
        color = colors.surfaceContainerLowest,
        border = border
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (digit != null) {
                Text(digit.toString(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            } else if (showCaret) {
                Box(Modifier.width(2.dp).height(28.dp).background(colors.primary))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun CodeInputPreview() {
    EchnoTheme {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CodeInput(value = "048", onValueChange = {}, length = 5, label = "Invitation code")
            CodeInput(value = "11111", onValueChange = {}, length = 5, label = "Invitation code", error = "No organization uses this code")
        }
    }
}
