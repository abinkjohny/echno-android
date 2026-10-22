package org.echno.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// How the Material roles map onto the brand: primary = navy (links, secondary buttons),
// secondary = amber (the one main action per screen), tertiary = green (done, on track),
// surface = paper, surfaceContainerLowest = the white cards on it.

private val LightColorScheme =
    lightColorScheme(
        primary = EchnoNavy,
        onPrimary = Color.White,
        primaryContainer = NavyTint,
        onPrimaryContainer = EchnoNavy,
        secondary = DuskAmber,
        onSecondary = SiteInk,
        secondaryContainer = AmberTint,
        onSecondaryContainer = AmberText,
        tertiary = SiteGreen,
        onTertiary = Color.White,
        background = DrawingPaper,
        onBackground = SiteInk,
        surface = DrawingPaper,
        onSurface = SiteInk,
        surfaceVariant = PaperTrack,
        onSurfaceVariant = InkMuted,
        surfaceContainerLowest = PaperCard,
        surfaceContainerLow = PaperCard,
        surfaceContainer = PaperCard,
        surfaceContainerHigh = PaperCard,
        surfaceContainerHighest = PaperTrack,
        outline = FieldOutline,
        outlineVariant = PaperRule,
        error = SiteRed,
        onError = Color.White
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = NightLink,
        onPrimary = NightSky,
        primaryContainer = NightNavyTint,
        onPrimaryContainer = NightOnNavyTint,
        secondary = NightAmber,
        onSecondary = SiteInk,
        secondaryContainer = NightAmberTint,
        onSecondaryContainer = NightAmber,
        tertiary = NightGreen,
        onTertiary = NightSky,
        background = NightSky,
        onBackground = NightInk,
        surface = NightSky,
        onSurface = NightInk,
        surfaceVariant = NightTrack,
        onSurfaceVariant = NightInkMuted,
        surfaceContainerLowest = NightSurface,
        surfaceContainerLow = NightSurface,
        surfaceContainer = NightSurface,
        surfaceContainerHigh = NightTrack,
        surfaceContainerHighest = NightTrack,
        outline = NightOutline,
        outlineVariant = NightRule,
        error = NightRed,
        onError = NightSky
    )

/**
 * Brand colours Material has no role for: the navy header band at the top of main screens and
 * what sits on it, the low sun, amber used as text, the hairline between rows in a card, and
 * the initials disc on the header.
 */
@Immutable
data class EchnoColors(
    val header: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val headerChip: Color,
    val headerOutline: Color,
    val sun: Color,
    val amberText: Color,
    val cardRule: Color,
    val avatar: Color,
    val onAvatar: Color
)

private val LightEchnoColors =
    EchnoColors(
        header = EchnoNavy,
        onHeader = Color.White,
        onHeaderMuted = OnHeaderMuted,
        headerChip = HeaderChip,
        headerOutline = HeaderOutline,
        sun = DuskAmber,
        amberText = AmberText,
        cardRule = PaperTrack,
        avatar = DrawingPaper,
        onAvatar = EchnoNavy
    )

private val DarkEchnoColors =
    EchnoColors(
        header = NightNavy,
        onHeader = Color.White,
        onHeaderMuted = OnHeaderMuted,
        headerChip = NightHeaderChip,
        headerOutline = HeaderOutline,
        sun = NightAmber,
        amberText = NightAmber,
        cardRule = NightRule,
        avatar = NightInk,
        onAvatar = EchnoNavy
    )

private val LocalEchnoColors = staticCompositionLocalOf { LightEchnoColors }

/** Read the brand colours as `EchnoTheme.colors.header`, next to `MaterialTheme.colorScheme`. */
object EchnoTheme {
    val colors: EchnoColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEchnoColors.current
}

/**
 * The app theme. Android 12+'s wallpaper-based dynamic colour is deliberately not used: the
 * brand colours carry meaning (amber = the main action), which a wallpaper palette would lose.
 */
@Composable
fun EchnoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalEchnoColors provides if (darkTheme) DarkEchnoColors else LightEchnoColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
