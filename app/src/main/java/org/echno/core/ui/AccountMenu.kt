package org.echno.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.echno.R

/**
 * The ⋮ menu in a signed-in screen's top bar. Screens don't know about sessions: navigation hands
 * this in through the screen's `topBarActions` slot.
 */
@Composable
fun AccountMenu(canSwitchOrganization: Boolean, onSwitchOrganization: () -> Unit, onSignOut: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.account_menu))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (canSwitchOrganization) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_switch_organization)) },
                    onClick = {
                        expanded = false
                        onSwitchOrganization()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_sign_out)) },
                onClick = {
                    expanded = false
                    onSignOut()
                }
            )
        }
    }
}
