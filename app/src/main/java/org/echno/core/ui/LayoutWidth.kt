package org.echno.core.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass

/**
 * How much room the window gives a screen, in Android's standard width buckets:
 * - [Compact]: under 600dp, a phone held upright.
 * - [Medium]: 600–840dp, a tablet held upright, a foldable opened, a phone on its side.
 * - [Expanded]: 840dp and up, a tablet on its side or a desktop window.
 *
 * Screens choose a layout from this, not from "is it a tablet": the same tablet is Medium upright
 * and Expanded on its side, and a split-screen window can be Compact on any device.
 */
enum class LayoutWidth { Compact, Medium, Expanded }

/** The current window's [LayoutWidth]. Updates when the window is rotated or resized. */
@Composable
fun currentLayoutWidth(): LayoutWidth {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> LayoutWidth.Expanded
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> LayoutWidth.Medium
        else -> LayoutWidth.Compact
    }
}

/** Previews a screen on a 1280 × 800dp landscape tablet, the size the tablet designs are drawn at. */
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(
    name = "Tablet, dark",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
annotation class TabletPreviews
