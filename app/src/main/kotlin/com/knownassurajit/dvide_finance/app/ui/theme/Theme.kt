package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.SwipeConfig
import com.knownassurajit.dvide_finance.app.util.formatMoney
import com.knownassurajit.dvide_finance.app.util.moneyParts

// ─────────────────────────────────────────────────────────────
// Extended token set — augments MaterialTheme for Dvide.
// Access via: LocalDvideColors.current
// ─────────────────────────────────────────────────────────────
data class DvideExtraColors(
    val status: Color,
    val statusContainer: Color,
    val onStatusContainer: Color,
    val seedHue: Int,
    val dark: Boolean,
    val dynamicColor: Boolean = false,
) {
    fun categoryColor(cat: String): Color =
        if (!dynamicColor) {
            if (dark) Category.monochromeDark(cat) else Category.monochromeLight(cat)
        } else {
            if (dark) Category.colorDark(cat, seedHue) else Category.colorLight(cat, seedHue)
        }

    fun categorySoft(cat: String): Color =
        if (!dynamicColor) {
            if (dark) Category.monochromeSoftDark(cat) else Category.monochromeSoftLight(cat)
        } else {
            if (dark) Category.softDark(cat, seedHue) else Category.softLight(cat, seedHue)
        }
}

val LocalDvideColors = staticCompositionLocalOf<DvideExtraColors> {
    error("No DvideExtraColors provided")
}

val MaterialTheme.dvideColors: DvideExtraColors
    @Composable @ReadOnlyComposable
    get() = LocalDvideColors.current

data class CurrencyFormatter(
    val currencyCode: String = "GBP",
    val regionCode: String = "GB",
    val weekStartDay: Int = 2,
    val numberFormat: String = "DEFAULT",
    val showDecimals: Boolean = true,
) {
    fun format(amount: Double, decimals: Int = if (showDecimals) 2 else 0): String {
        return amount.formatMoney(currencyCode, regionCode, numberFormat, decimals, showDecimals)
    }

    fun parts(amount: Double): com.knownassurajit.dvide_finance.app.util.MoneyParts {
        return amount.moneyParts(currencyCode, regionCode, numberFormat, showDecimals)
    }
}

val LocalCurrencyFormatter = staticCompositionLocalOf<CurrencyFormatter> {
    CurrencyFormatter()
}

/** User-configured swipe mapping for ledger rows (provided from the persisted settings). */
val LocalSwipeConfig = staticCompositionLocalOf { SwipeConfig.DEFAULT }

/** User-configured dashboard card visual style. */
val LocalDashboardStyle = staticCompositionLocalOf { com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle.DEFAULT }

val MaterialTheme.dashboardStyle: com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle
    @Composable @ReadOnlyComposable
    get() = LocalDashboardStyle.current

data class DashboardCardColors(
    val containerColor: Color,
    val borderColor: Color,
)

@Composable
@ReadOnlyComposable
fun dashboardCardColors(style: com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle = MaterialTheme.dashboardStyle): DashboardCardColors {
    return when (style) {
        com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle.TINTED -> DashboardCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
        )
        com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle.MINIMAL -> DashboardCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
        )
        com.knownassurajit.dvide_finance.app.domain.model.DashboardStyle.CONTAINER -> DashboardCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            borderColor = Color.Transparent,
        )
    }
}

@Composable
fun DvideTheme(
    seedHue: Int   = 300,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    currencyCode: String = "GBP",
    regionCode: String = "GB",
    weekStartDay: Int = 2,
    numberFormat: String = "DEFAULT",
    showDecimals: Boolean = true,
    fontOption: AppFont = AppFont.GOOGLE_SANS,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (dynamicColor) {
        colorSchemeForHue(seedHue, darkTheme)
    } else {
        monochromeColorScheme(darkTheme)
    }

    val extraColors = DvideExtraColors(
        status           = colorScheme.error,
        statusContainer  = colorScheme.errorContainer,
        onStatusContainer = colorScheme.onErrorContainer,
        seedHue          = seedHue,
        dark             = darkTheme,
        dynamicColor     = dynamicColor,
    )

    val formatter = CurrencyFormatter(currencyCode, regionCode, weekStartDay, numberFormat, showDecimals)

    CompositionLocalProvider(
        LocalDvideColors provides extraColors,
        LocalCurrencyFormatter provides formatter,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typographyFor(fontOption),
            shapes      = DvideShapes,
            content     = content,
        )
    }
}
