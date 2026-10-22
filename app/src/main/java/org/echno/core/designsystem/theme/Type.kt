package org.echno.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.echno.R

/**
 * Echno Serif: Merriweather Italic (Light), cut down to Latin and renamed because "Merriweather"
 * is a Reserved Font Name under its SIL Open Font License. Licence: assets/licenses/echno_serif_OFL.txt.
 */
val EchnoSerif = FontFamily(
    Font(R.font.echno_serif_italic, weight = FontWeight.Light, style = FontStyle.Italic)
)

// Bricolage Grotesque (headings) and Atkinson Hyperlegible Next (everything else) are variable
// fonts: one file holds every weight, and each Font below picks a point on the weight axis.
// Both are SIL Open Font License with no reserved names, bundled unmodified.
// Licences: assets/licenses/bricolage_grotesque_OFL.txt, atkinson_hyperlegible_next_OFL.txt.

private val SemiBold650 = FontWeight(650)
private val ExtraBold750 = FontWeight(750)

@OptIn(ExperimentalTextApi::class)
private fun bricolage(weight: FontWeight) =
    Font(R.font.bricolage_grotesque, weight = weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

@OptIn(ExperimentalTextApi::class)
private fun atkinson(weight: FontWeight) = Font(
    R.font.atkinson_hyperlegible_next,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val Bricolage = FontFamily(bricolage(FontWeight.Medium), bricolage(SemiBold650), bricolage(FontWeight.Bold), bricolage(ExtraBold750))

val Atkinson = FontFamily(atkinson(FontWeight.Normal), atkinson(FontWeight.Medium), atkinson(FontWeight.Bold))

/** Brand tagline ("Where Construction Meets Intelligent Management"). Taglines and welcome lines only. */
val TaglineTextStyle = TextStyle(
    fontFamily = EchnoSerif,
    fontWeight = FontWeight.Light,
    fontStyle = FontStyle.Italic,
    fontSize = 17.sp,
    lineHeight = 26.sp
)

/** Numbered section headers ("01 CONTACT"): spaced capitals, like the title block of a drawing. */
val SectionTitleTextStyle = TextStyle(
    fontFamily = Atkinson,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.08.em
)

val SectionNumberTextStyle = TextStyle(
    fontFamily = Bricolage,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 20.sp
)

private fun heading(size: Int, line: Int, weight: FontWeight = ExtraBold750, tracking: Double = 0.0) = TextStyle(
    fontFamily = Bricolage,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Atkinson,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

// Body text is 17sp and nothing is smaller than 14sp, so text stays readable at arm's length on site.
val Typography = Typography(
    displayLarge = heading(60, 64, tracking = -0.02),
    displayMedium = heading(40, 44, tracking = -0.02),
    displaySmall = heading(34, 40, tracking = -0.01),
    headlineLarge = heading(36, 40, tracking = -0.01),
    headlineMedium = heading(30, 34, tracking = -0.01),
    headlineSmall = heading(28, 34, tracking = -0.01),
    titleLarge = heading(22, 28, weight = SemiBold650),
    titleMedium = heading(20, 26, weight = SemiBold650),
    titleSmall = body(17, 24, weight = FontWeight.Bold),
    bodyLarge = body(17, 26),
    bodyMedium = body(15, 22),
    bodySmall = body(14, 20),
    labelLarge = body(17, 24, weight = FontWeight.Bold),
    labelMedium = body(15, 20, weight = FontWeight.Bold),
    labelSmall = body(14, 20, weight = FontWeight.Medium)
)
