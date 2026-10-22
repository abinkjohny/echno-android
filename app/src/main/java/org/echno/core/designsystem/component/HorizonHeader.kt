package org.echno.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.echno.R
import org.echno.core.designsystem.theme.EchnoTheme

/**
 * The navy band at the top of a main screen, ending in the curve of the echno "e" with a low
 * amber sun behind it. Draws behind the status bar, so its content is padded below it.
 * Only at the top of main screens; focused pages (forms) use [PageHeader].
 */
@Composable
fun HorizonHeader(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = EchnoTheme.colors
    Column(modifier.fillMaxWidth().background(colors.header)) {
        CompositionLocalProvider(LocalContentColor provides colors.onHeader) {
            Column(
                modifier = Modifier.statusBarsPadding().padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                content = content
            )
        }
        Horizon(sun = true, height = 64.dp)
    }
}

/**
 * The curve under a header. [sun] adds the amber sun rising behind it; [ground] is the colour of
 * what lies below the curve (the screen's background by default).
 */
@Composable
fun Horizon(
    sun: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = if (sun) 64.dp else 32.dp,
    ground: Color = MaterialTheme.colorScheme.background,
    sunColor: Color = EchnoTheme.colors.sun
) {
    // Decorative: Canvas has no semantics, so TalkBack skips it. Clipped, so only the part of the
    // sun above the horizon shows instead of the rest spilling onto what's below.
    Canvas(modifier.fillMaxWidth().height(height).clipToBounds()) {
        val w = size.width
        val h = size.height
        if (sun) drawCircle(sunColor, radius = 38.dp.toPx(), center = Offset(w * 0.815f, h * 0.94f))
        val curve =
            Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.72f)
                quadraticTo(w / 2, -h * 0.1f, w, h * 0.72f)
                lineTo(w, h)
                close()
            }
        drawPath(curve, ground)
    }
}

/**
 * The header of a focused page (a form): a way back, the title, and the curve without the sun.
 * [compact] is the phone version, a back link above the title; otherwise (tablets) a labelled
 * "Cancel"-style pill sits beside the title.
 */
@Composable
fun PageHeader(
    title: String,
    backLabel: String,
    backIcon: Painter,
    onBack: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val colors = EchnoTheme.colors
    Column(modifier.fillMaxWidth().background(colors.header)) {
        CompositionLocalProvider(LocalContentColor provides colors.onHeader) {
            if (compact) {
                Column(
                    Modifier.statusBarsPadding().padding(start = 12.dp, top = 4.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onHeader),
                        contentPadding = PaddingValues(start = 4.dp, end = 12.dp)
                    ) {
                        Icon(backIcon, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(backLabel, style = MaterialTheme.typography.labelMedium)
                    }
                    PageTitle(title, subtitle, Modifier.padding(horizontal = 8.dp))
                }
            } else {
                Row(
                    modifier = Modifier.statusBarsPadding().padding(start = 40.dp, top = 20.dp, end = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.heightIn(min = 48.dp),
                        border = BorderStroke(1.dp, colors.headerOutline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onHeader),
                        contentPadding = PaddingValues(start = 12.dp, end = 18.dp)
                    ) {
                        Icon(backIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(backLabel, style = MaterialTheme.typography.labelLarge)
                    }
                    PageTitle(title, subtitle)
                }
            }
        }
        Horizon(sun = false, height = if (compact) 32.dp else 40.dp, modifier = Modifier.padding(top = 8.dp))
    }
}

/** A labelled, outlined pill for actions on the navy header ("Account", "Sign Out"). */
@Composable
fun HeaderPillButton(text: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = EchnoTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        border = BorderStroke(1.dp, colors.headerOutline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onHeader),
        contentPadding = PaddingValues(start = 10.dp, end = 14.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EchnoTheme.colors.onHeaderMuted)
        }
        Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun HorizonHeaderPreview() {
    EchnoTheme {
        HorizonHeader {
            Text("Profile", style = MaterialTheme.typography.titleLarge)
            Text("Asha Menon", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
private fun PageHeaderPreview() {
    EchnoTheme {
        PageHeader(
            title = "Create Account",
            backLabel = "Back",
            backIcon = painterResource(R.drawable.ic_chevron_back),
            onBack = {},
            compact = true
        )
    }
}
