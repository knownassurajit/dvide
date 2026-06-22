package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// M3 Expressive type scale — mirrors the web design's Roboto Flex scale.
// Android ships Roboto as the system font; FontFamily.Default resolves to it.
// For the production build, bundle RobotoFlex-VariableFont.ttf in res/font/ and
// use a custom FontFamily with the variable axes (wght 100–1000, wdth 25–151).

val DvideTypography = Typography(
    // displayLarge → huge editorial numeral
    displayLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 88.sp,
        lineHeight   = 80.sp,
        letterSpacing = (-4).sp,
    ),
    // displayMedium → large gauge number
    displayMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 52.sp,
        lineHeight   = 52.sp,
        letterSpacing = (-1.5).sp,
    ),
    // displaySmall → cards hero number
    displaySmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.ExtraBold,
        fontSize     = 60.sp,
        lineHeight   = 58.sp,
        letterSpacing = (-2.5).sp,
    ),
    // headlineLarge / Medium / Small
    headlineLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 32.sp,
        lineHeight   = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 32.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 25.sp,
        lineHeight   = 28.sp,
        letterSpacing = (-0.4).sp,
    ),
    // titleLarge → greeting name, sheet title
    titleLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight(520),
        fontSize     = 21.sp,
        lineHeight   = 25.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 16.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp,
    ),
    // bodyLarge → transaction notes
    bodyLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.5.sp,
        lineHeight   = 22.sp,
        letterSpacing = (-0.05).sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.sp,
    ),
    // labelLarge → segmented control, button labels
    labelLarge = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight(560),
        fontSize     = 14.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.sp,
    ),
    // labelMedium → cycle label, category tags
    labelMedium = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight(560),
        fontSize     = 12.5.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.sp,
    ),
    // labelSmall → smallest metadata
    labelSmall = TextStyle(
        fontFamily   = FontFamily.Default,
        fontWeight   = FontWeight(560),
        fontSize     = 11.sp,
        lineHeight   = 14.sp,
        letterSpacing = 0.sp,
    ),
)
