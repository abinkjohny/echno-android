package org.echno.feature.auth.ui.welcome

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.echno.R
import org.echno.core.designsystem.component.ButtonHeight
import org.echno.core.designsystem.component.OutlineButton
import org.echno.core.designsystem.component.PrimaryButton
import org.echno.core.designsystem.component.TabletButtonHeight
import org.echno.core.designsystem.theme.Bricolage
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.SplashTaglineDark
import org.echno.core.designsystem.theme.SplashTaglineLight
import org.echno.core.designsystem.theme.TaglineTextStyle
import org.echno.core.ui.LayoutWidth
import org.echno.core.ui.TabletPreviews
import org.echno.core.ui.currentLayoutWidth

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
fun WelcomeScreen(
    notice: WelcomeNotice?,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
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
        onCreateAccount = onCreateAccount,
        modifier = modifier
    )
}

/**
 * Phones (and tablets held upright): the splash artwork full screen with everything over its sky.
 * Tablets on their side: the artwork and headline on the left, the welcome and buttons on the right.
 */
@Composable
fun WelcomeContent(
    uiState: WelcomeUiState,
    notice: WelcomeNotice?,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
    layoutWidth: LayoutWidth = currentLayoutWidth()
) {
    // A Surface rather than a plain background, so text on it gets the matching content colour.
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (layoutWidth == LayoutWidth.Expanded) {
            WideWelcome(uiState, notice, onSignIn, onCreateAccount)
        } else {
            NarrowWelcome(uiState, notice, onSignIn, onCreateAccount)
        }
    }
}

@Composable
private fun WideWelcome(uiState: WelcomeUiState, notice: WelcomeNotice?, onSignIn: () -> Unit, onCreateAccount: () -> Unit) {
    Row {
        Box(Modifier.weight(0.5625f).fillMaxHeight()) {
            Artwork()
            // On a wide pane the cranes reach up behind the headline; fade the sky in behind the text.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        0.45f to MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                        0.7f to Color.Transparent
                    )
                )
            )
            // Scrolls like the right pane, for a short window or enlarged text.
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(horizontal = 64.dp, vertical = 56.dp)
            ) {
                BrandMark(tile = 52.dp)
                Spacer(Modifier.height(96.dp))
                Headline(MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(18.dp))
                Tagline()
            }
        }
        Column(
            modifier =
            Modifier
                .weight(0.4375f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 72.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                stringResource(R.string.welcome_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displaySmall
            )
            Text(stringResource(R.string.welcome_message), style = MaterialTheme.typography.bodyLarge, fontSize = 18.sp, lineHeight = 27.sp)
            Spacer(Modifier.height(4.dp))
            Actions(uiState, notice, onSignIn, onCreateAccount, TabletButtonHeight, outlineColor = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NarrowWelcome(uiState: WelcomeUiState, notice: WelcomeNotice?, onSignIn: () -> Unit, onCreateAccount: () -> Unit) {
    Box {
        // Same artwork as the splash, so the hand-over from the splash is seamless.
        Artwork()
        Column(
            modifier =
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxSize()
                // Scrolls when the text is enlarged; at normal sizes everything fits above the skyline.
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            BrandMark(tile = 44.dp)
            Spacer(Modifier.height(72.dp))
            Headline(MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(14.dp))
            Tagline()
            Spacer(Modifier.height(40.dp))
            Actions(uiState, notice, onSignIn, onCreateAccount, ButtonHeight, outlineColor = taglineColor())
        }
    }
}

@Composable
private fun Artwork() {
    Image(
        painter = painterResource(R.drawable.splash_background),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        // Fill any aspect ratio, cropping the sky rather than the skyline.
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter
    )
}

/** The echno "e" on a white tile, then the wordmark. */
@Composable
private fun BrandMark(tile: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(modifier = Modifier.size(tile), shape = MaterialTheme.shapes.small, color = Color.White) {
            Image(painterResource(R.drawable.echno_mark), contentDescription = null, modifier = Modifier.padding(tile / 10))
        }
        Text(
            stringResource(R.string.splash_logo_description).lowercase(),
            fontFamily = Bricolage,
            fontWeight = FontWeight.Bold,
            fontSize = if (tile > 48.dp) 28.sp else 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun Headline(style: TextStyle) {
    Text(
        stringResource(R.string.welcome_headline),
        modifier = Modifier.semantics { heading() },
        style = style,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun Tagline() {
    Text(stringResource(R.string.splash_tagline), style = TaglineTextStyle, color = taglineColor())
}

@Composable
private fun taglineColor(): Color = if (isSystemInDarkTheme()) SplashTaglineDark else SplashTaglineLight

@Composable
private fun Actions(
    uiState: WelcomeUiState,
    notice: WelcomeNotice?,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    buttonHeight: Dp,
    outlineColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NoticeCard(notice, uiState.errorRes)
        PrimaryButton(
            stringResource(R.string.action_sign_in),
            onClick = onSignIn,
            loading = uiState.isSigningIn,
            height = buttonHeight,
            modifier = Modifier.fillMaxWidth()
        )
        OutlineButton(
            stringResource(R.string.action_create_account),
            onClick = onCreateAccount,
            enabled = !uiState.isSigningIn,
            height = buttonHeight,
            contentColor = MaterialTheme.colorScheme.onBackground,
            borderColor = outlineColor,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(R.string.welcome_secure_note),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = taglineColor(),
            textAlign = TextAlign.Center
        )
    }
}

/** A notice (account created, session ended) or a sign-in error, on a card so it reads over the artwork. */
@Composable
private fun NoticeCard(notice: WelcomeNotice?, errorRes: Int?) {
    val text =
        when {
            errorRes != null -> stringResource(errorRes)
            notice is WelcomeNotice.AccountCreated -> stringResource(R.string.welcome_account_created, notice.username)
            notice == WelcomeNotice.SessionExpired -> stringResource(R.string.error_unauthorized)
            else -> return
        }
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (errorRes != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "Light", showSystemUi = true)
@Preview(name = "Dark", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomePreview() {
    EchnoTheme {
        WelcomeContent(uiState = WelcomeUiState(), notice = null, onSignIn = {}, onCreateAccount = {})
    }
}

@Preview(name = "Session expired", showSystemUi = true)
@Composable
private fun WelcomeExpiredPreview() {
    EchnoTheme {
        WelcomeContent(uiState = WelcomeUiState(), notice = WelcomeNotice.SessionExpired, onSignIn = {}, onCreateAccount = {})
    }
}

@TabletPreviews
@Composable
private fun WelcomeTabletPreview() {
    EchnoTheme {
        WelcomeContent(uiState = WelcomeUiState(), notice = null, onSignIn = {}, onCreateAccount = {}, layoutWidth = LayoutWidth.Expanded)
    }
}
