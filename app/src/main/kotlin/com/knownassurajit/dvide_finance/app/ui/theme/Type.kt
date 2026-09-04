package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.R

// Brand (Outfit) for display / headline / title — geometric, high-impact numerals.
// Plain (Manrope) for body / label — readable at small sizes.
// Both are OFL Google Fonts, bundled so the scale never flashes to device default.

@OptIn(ExperimentalTextApi::class)
private fun outfit(weight: FontWeight) = Font(
    resId = R.font.outfit,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun manrope(weight: FontWeight) = Font(
    resId = R.font.manrope,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val OutfitFamily = FontFamily(
    outfit(FontWeight.Light),
    outfit(FontWeight.Normal),
    outfit(FontWeight.Medium),
    outfit(FontWeight.SemiBold),
    outfit(FontWeight.Bold),
    outfit(FontWeight.ExtraBold),
)

val ManropeFamily = FontFamily(
    manrope(FontWeight.Light),
    manrope(FontWeight.Normal),
    manrope(FontWeight.Medium),
    manrope(FontWeight.SemiBold),
    manrope(FontWeight.Bold),
    manrope(FontWeight.ExtraBold),
)

private val NoFontPad = PlatformTextStyle(includeFontPadding = false)

private val CenteredLines = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun type(
    family: FontFamily,
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    features: String? = null,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = features,
    platformStyle = NoFontPad,
    lineHeightStyle = CenteredLines,
)

private const val Tabular = "tnum, lnum"

val DvideTypography = Typography(
    displayLarge = type(OutfitFamily, FontWeight.Bold, 48.sp, 52.sp, (-1.2).sp, Tabular),
    displayMedium = type(OutfitFamily, FontWeight.Bold, 40.sp, 44.sp, (-0.8).sp, Tabular),
    displaySmall = type(OutfitFamily, FontWeight.Bold, 32.sp, 36.sp, (-0.4).sp, Tabular),
    headlineLarge = type(OutfitFamily, FontWeight.SemiBold, 32.sp, 40.sp, 0.sp, Tabular),
    headlineMedium = type(OutfitFamily, FontWeight.SemiBold, 28.sp, 36.sp, 0.sp),
    headlineSmall = type(OutfitFamily, FontWeight.SemiBold, 24.sp, 32.sp, 0.sp, Tabular),
    titleLarge = type(OutfitFamily, FontWeight.SemiBold, 22.sp, 28.sp, 0.sp),
    titleMedium = type(OutfitFamily, FontWeight.Medium, 16.sp, 24.sp, 0.15.sp),
    titleSmall = type(OutfitFamily, FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    bodyLarge = type(ManropeFamily, FontWeight.Normal, 16.sp, 24.sp, 0.15.sp),
    bodyMedium = type(ManropeFamily, FontWeight.Normal, 14.sp, 20.sp, 0.25.sp),
    bodySmall = type(ManropeFamily, FontWeight.Normal, 12.sp, 16.sp, 0.4.sp),
    labelLarge = type(ManropeFamily, FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
    labelMedium = type(ManropeFamily, FontWeight.SemiBold, 12.sp, 16.sp, 0.5.sp),
    labelSmall = type(ManropeFamily, FontWeight.SemiBold, 11.sp, 16.sp, 0.5.sp),
)
