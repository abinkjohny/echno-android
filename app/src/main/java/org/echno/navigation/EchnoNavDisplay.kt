package org.echno.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.echno.core.auth.SessionState
import org.echno.feature.auth.ui.welcome.WelcomeNotice
import org.echno.feature.auth.ui.welcome.WelcomeScreen
import org.echno.feature.home.ui.HomeScreen
import org.echno.feature.splash.ui.SplashScreen

/**
 * App navigation host. The back stack is a plain list we own: push a route with
 * `backStack.add(route)`, go back with `backStack.removeLastOrNull()`.
 * [NavDisplay] shows the last entry and handles the system back gesture.
 *
 * The [sessionState] decides the bottom of the stack (the "root"): Welcome when signed out, Home
 * when signed in. Screens push on top of it. When the session changes (sign-in, sign-out, an expired
 * session), the stack is replaced, so Back can never lead to a screen of the previous session.
 */
@Composable
fun EchnoNavDisplay(sessionState: SessionState, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Splash)
    // Saveable: survives rotation and process death together with the back stack.
    var splashFinished by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(splashFinished, sessionState) {
        if (!splashFinished) return@LaunchedEffect
        val root = rootFor(sessionState) ?: return@LaunchedEffect // still reading the stored session
        if (backStack.firstOrNull()?.javaClass != root.javaClass) {
            backStack.clear()
            backStack.add(root)
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
        listOf(
            // Keeps each screen's remembered UI state while it is on the stack.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Gives each screen its own ViewModels, cleared when the screen is popped.
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider =
        entryProvider {
            // The splash stays up until both its minimum time has passed and the session is known;
            // the LaunchedEffect above then replaces it, so Back never returns to it.
            entry<Splash> { SplashScreen(onFinished = { splashFinished = true }) }
            entry<Welcome> { route ->
                val notice =
                    when {
                        route.registeredUsername != null -> WelcomeNotice.AccountCreated(route.registeredUsername)
                        (sessionState as? SessionState.SignedOut)?.sessionExpired == true -> WelcomeNotice.SessionExpired
                        else -> null
                    }
                WelcomeScreen(notice = notice)
            }
            entry<Home> { HomeScreen() }
        }
    )
}

/** The screen at the bottom of the stack for [state]; null while the stored session is being read. */
private fun rootFor(state: SessionState): NavKey? = when (state) {
    SessionState.Loading -> null
    is SessionState.SignedOut -> Welcome()
    is SessionState.SignedIn -> Home
}
