package com.dvide.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.dvide.app.data.model.Category
import com.dvide.app.util.formatMoney
import com.dvide.app.util.moneyParts

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
) {
    fun categoryColor(cat: String): Color =
        if (dark) Category.colorDark(cat, seedHue) else Category.colorLight(cat, seedHue)

    fun categorySoft(cat: String): Color =
        if (dark) Category.softDark(cat, seedHue) else Category.softLight(cat, seedHue)
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
    val numberFormat: String = "DEFAULT"
) {
    fun format(amount: Double, decimals: Int = 0): String {
        return amount.formatMoney(currencyCode, regionCode, numberFormat, decimals)
    }

    fun parts(amount: Double): com.dvide.app.util.MoneyParts {
        return amount.moneyParts(currencyCode, regionCode, numberFormat)
    }
}

val LocalCurrencyFormatter = staticCompositionLocalOf<CurrencyFormatter> {
    CurrencyFormatter()
}

@Composable
fun DvideTheme(
    seedHue: Int   = 300,
    darkTheme: Boolean = true,
    currencyCode: String = "GBP",
    regionCode: String = "GB",
    weekStartDay: Int = 2,
    numberFormat: String = "DEFAULT",
    content: @Composable () -> Unit,
) {
    val colorScheme = colorSchemeForHue(seedHue, darkTheme)

    val extraColors = DvideExtraColors(
        status           = colorScheme.error,
        statusContainer  = colorScheme.errorContainer,
        onStatusContainer = colorScheme.onErrorContainer,
        seedHue          = seedHue,
        dark             = darkTheme,
    )

    val formatter = CurrencyFormatter(currencyCode, regionCode, weekStartDay, numberFormat)

    CompositionLocalProvider(
        LocalDvideColors provides extraColors,
        LocalCurrencyFormatter provides formatter
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = DvideTypography,
            shapes      = DvideShapes,
            content     = content,
        )
    }
}
