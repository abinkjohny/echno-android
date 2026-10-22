package org.echno.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.echno.R
import org.echno.core.designsystem.component.HeaderPillButton

/**
 * What a signed-in screen can do with the account. Screens don't know about sessions: navigation
 * hands this in, and each screen shows it as fits its layout (a menu on phones, buttons on tablets).
 */
data class AccountActions(val canSwitchOrganization: Boolean, val onSwitchOrganization: () -> Unit, val onSignOut: () -> Unit) {
    companion object {
        /** For previews and tests. */
        val None = AccountActions(canSwitchOrganization = false, onSwitchOrganization = {}, onSignOut = {})
    }
}

/** The labelled "Account" pill on a header, opening the account menu. */
@Composable
fun AccountMenu(actions: AccountActions) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        HeaderPillButton(stringResource(R.string.account_menu), painterResource(R.drawable.ic_account), onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (actions.canSwitchOrganization) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_switch_organization), style = MaterialTheme.typography.bodyLarge) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_organization), contentDescription = null) },
                    onClick = {
                        expanded = false
                        actions.onSwitchOrganization()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_sign_out), style = MaterialTheme.typography.bodyLarge) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_sign_out), contentDescription = null) },
                onClick = {
                    expanded = false
                    actions.onSignOut()
                }
            )
        }
    }
}
