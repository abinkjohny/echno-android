package org.echno.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.echno.R

/**
 * Echno Serif: Merriweather Italic (Light), cut down to Latin and renamed because "Merriweather"
 * is a Reserved Font Name under its SIL Open Font License. Licence: assets/licenses/echno_serif_OFL.txt.
 */
val EchnoSerif = FontFamily(
    Font(R.font.echno_serif_italic, weight = FontWeight.Light, style = FontStyle.Italic)
)

/** Brand tagline ("Where Construction Meets Intelligent Management"). */
val TaglineTextStyle = TextStyle(
    fontFamily = EchnoSerif,
    fontWeight = FontWeight.Light,
    fontStyle = FontStyle.Italic,
    fontSize = 18.sp,
    lineHeight = 26.sp
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
)
