package org.echno.core.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import timber.log.Timber

/**
 * Hands a phone number to the dialler or an address to the mail app. Dialling only fills in the
 * number (ACTION_DIAL), so no call permission is needed and the user still presses call.
 * A tablet often has no dialler: check [canDial] / [canEmail] before offering the action.
 */
class ContactLauncher(private val context: Context) {
    val canDial: Boolean = dialIntent("0").resolveActivity(context.packageManager) != null
    val canEmail: Boolean = emailIntent("a@b.c").resolveActivity(context.packageManager) != null

    fun dial(number: String) = start(dialIntent(number))

    fun email(address: String) = start(emailIntent(address))

    private fun dialIntent(number: String) = Intent(Intent.ACTION_DIAL, "tel:${number.filter { it.isDigit() || it == '+' }}".toUri())

    private fun emailIntent(address: String) = Intent(Intent.ACTION_SENDTO, "mailto:$address".toUri())

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // The app was uninstalled between the check and the tap.
            Timber.w(e, "No app for %s", intent.action)
        }
    }
}

@Composable
fun rememberContactLauncher(): ContactLauncher {
    val context = LocalContext.current
    return remember(context) { ContactLauncher(context) }
}
