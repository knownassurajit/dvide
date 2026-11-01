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
import com.knownassurajit.dvide_finance.app.domain.model.AppFont

@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val GoogleSansFamily = FontFamily(
    variableFont(R.font.google_sans_variable, FontWeight.Normal),
    variableFont(R.font.google_sans_variable, FontWeight.Medium),
    variableFont(R.font.google_sans_variable, FontWeight.SemiBold),
    variableFont(R.font.google_sans_variable, FontWeight.Bold),
)

val DmSansFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

val GeistFamily = FontFamily(
    variableFont(R.font.geist_variable, FontWeight.Normal),
    variableFont(R.font.geist_variable, FontWeight.Medium),
    variableFont(R.font.geist_variable, FontWeight.SemiBold),
    variableFont(R.font.geist_variable, FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_bold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

fun fontFamilyFor(appFont: AppFont): FontFamily = when (appFont) {
    AppFont.GOOGLE_SANS       -> GoogleSansFamily
    AppFont.DM_SANS           -> DmSansFamily
    AppFont.GEIST             -> GeistFamily
    AppFont.INTER             -> InterFamily
    AppFont.MANROPE           -> ManropeFamily
    AppFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFamily
}

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

fun typographyFor(appFont: AppFont): Typography {
    val family = fontFamilyFor(appFont)
    return Typography(
        displayLarge = type(family, FontWeight.Bold, 48.sp, 52.sp, (-1.2).sp, Tabular),
        displayMedium = type(family, FontWeight.Bold, 40.sp, 44.sp, (-0.8).sp, Tabular),
        displaySmall = type(family, FontWeight.Bold, 32.sp, 36.sp, (-0.4).sp, Tabular),
        headlineLarge = type(family, FontWeight.SemiBold, 32.sp, 40.sp, 0.sp, Tabular),
        headlineMedium = type(family, FontWeight.SemiBold, 28.sp, 36.sp, 0.sp),
        headlineSmall = type(family, FontWeight.SemiBold, 24.sp, 32.sp, 0.sp, Tabular),
        titleLarge = type(family, FontWeight.SemiBold, 22.sp, 28.sp, 0.sp),
        titleMedium = type(family, FontWeight.Medium, 16.sp, 24.sp, 0.15.sp),
        titleSmall = type(family, FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
        bodyLarge = type(family, FontWeight.Normal, 16.sp, 24.sp, 0.15.sp),
        bodyMedium = type(family, FontWeight.Normal, 14.sp, 20.sp, 0.25.sp),
        bodySmall = type(family, FontWeight.Normal, 12.sp, 16.sp, 0.4.sp),
        labelLarge = type(family, FontWeight.SemiBold, 14.sp, 20.sp, 0.1.sp),
        labelMedium = type(family, FontWeight.SemiBold, 12.sp, 16.sp, 0.5.sp),
        labelSmall = type(family, FontWeight.SemiBold, 11.sp, 16.sp, 0.5.sp),
    )
}

val DvideTypography = typographyFor(AppFont.GOOGLE_SANS)
