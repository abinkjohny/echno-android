package org.echno.feature.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.echno.R
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.core.designsystem.theme.SplashTaglineDark
import org.echno.core.designsystem.theme.SplashTaglineLight

/** How long the branded splash stays up. Phase 3 replaces the fixed wait with the session check. */
private const val SPLASH_DURATION_MS = 2_500L

/**
 * Branded splash: the construction artwork full screen, with the logo and tagline in the
 * open sky area. Light/dark artwork comes from drawable-nodpi / drawable-night-nodpi.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    // Keeps the latest callback without restarting the timer if the caller passes a new lambda.
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        currentOnFinished()
    }

    SplashContent(modifier)
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    val taglineColor = if (isSystemInDarkTheme()) SplashTaglineDark else SplashTaglineLight

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null, // decorative
            modifier = Modifier.fillMaxSize(),
            // Fill the screen on any aspect ratio, cropping the sky rather than the skyline.
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter
        )
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo sits about a third of the way down, clear of the skyline.
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = stringResource(R.string.splash_logo_description),
                modifier = Modifier.size(168.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = taglineColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(2f))
        }
    }
}

@Preview(name = "Light", showSystemUi = true)
@Preview(name = "Dark", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SplashPreview() {
    EchnoTheme {
        SplashContent()
    }
}
