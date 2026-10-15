package org.echno.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.echno.feature.home.ui.HomeScreen

/**
 * App navigation host. The back stack is a plain list we own: push a route with
 * `backStack.add(route)`, go back with `backStack.removeLastOrNull()`.
 * [NavDisplay] shows the last entry and handles the system back gesture.
 */
@Composable
fun EchnoNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Home)

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
            entry<Home> { HomeScreen() }
        }
    )
}
