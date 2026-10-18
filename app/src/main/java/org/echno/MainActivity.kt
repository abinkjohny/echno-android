package org.echno

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.echno.core.designsystem.theme.EchnoTheme
import org.echno.navigation.EchnoNavDisplay

/**
 * The app's only Activity (the window the OS opens). Every screen is a composable
 * swapped in and out by [EchnoNavDisplay], not a separate Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: hands over from the system splash (Theme.Echno.Starting)
        // to the app theme.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
            val canSwitchOrganization by viewModel.canSwitchOrganization.collectAsStateWithLifecycle()
            EchnoTheme {
                EchnoNavDisplay(
                    sessionState = sessionState,
                    canSwitchOrganization = canSwitchOrganization,
                    onSwitchOrganization = viewModel::switchOrganization,
                    onSignOut = viewModel::signOut
                )
            }
        }
    }
}
