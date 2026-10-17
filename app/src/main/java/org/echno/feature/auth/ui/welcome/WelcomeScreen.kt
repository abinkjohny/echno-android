package org.echno.feature.auth.ui.welcome

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.echno.R
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.SplashTaglineDark
import org.echno.core.designsystem.theme.SplashTaglineLight
import org.echno.core.designsystem.theme.TaglineTextStyle

/** Why the user is here, shown above the buttons. */
sealed interface WelcomeNotice {
    data class AccountCreated(val username: String) : WelcomeNotice

    data object SessionExpired : WelcomeNotice
}

/**
 * The signed-out home. After registering, [notice] carries the new username, which pre-fills
 * Keycloak's login page.
 */
@Composable
fun WelcomeScreen(notice: WelcomeNotice?, modifier: Modifier = Modifier, viewModel: WelcomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Opens the login page in a Custom Tab and receives the result when the browser redirects back.
    // Registered with the Activity, so a result still arrives if Android recreated the screen meanwhile.
    val signInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.onSignInResult(result.data)
        }
    val loginHint = (notice as? WelcomeNotice.AccountCreated)?.username

    WelcomeContent(
        uiState = uiState,
        notice = notice,
        onSignIn = { viewModel.signInIntent(loginHint)?.let(signInLauncher::launch) },
        modifier = modifier
    )
}

@Composable
fun WelcomeContent(uiState: WelcomeUiState, notice: WelcomeNotice?, onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val taglineColor = if (isSystemInDarkTheme()) SplashTaglineDark else SplashTaglineLight

    Box(modifier.fillMaxSize()) {
        // Same artwork as the splash, so the hand-over from the splash is seamless.
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter
        )
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = stringResource(R.string.splash_logo_description),
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = TaglineTextStyle,
                color = taglineColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.weight(1f))
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.welcome_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    NoticeText(notice)
                    uiState.errorRes?.let { errorRes ->
                        Text(stringResource(errorRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = onSignIn, enabled = !uiState.isSigningIn, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        if (uiState.isSigningIn) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.action_sign_in))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeText(notice: WelcomeNotice?) {
    val text =
        when (notice) {
            is WelcomeNotice.AccountCreated -> stringResource(R.string.welcome_account_created, notice.username)
            WelcomeNotice.SessionExpired -> stringResource(R.string.error_unauthorized)
            null -> return
        }
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
}

@Preview(name = "Light", showSystemUi = true)
@Preview(name = "Dark", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomePreview() {
    EchnoTheme {
        WelcomeContent(uiState = WelcomeUiState(), notice = null, onSignIn = {})
    }
}

@Preview(name = "Session expired", showSystemUi = true)
@Composable
private fun WelcomeExpiredPreview() {
    EchnoTheme {
        WelcomeContent(uiState = WelcomeUiState(), notice = WelcomeNotice.SessionExpired, onSignIn = {})
    }
}
