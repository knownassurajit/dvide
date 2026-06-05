package com.dvide.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.dvide.app.data.model.Category

// ─────────────────────────────────────────────────────────────
// Extended token set — augments MaterialTheme for Cyclewise.
// Access via: LocalCyclewiseColors.current
// ─────────────────────────────────────────────────────────────
data class CyclewiseExtraColors(
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

val LocalCyclewiseColors = staticCompositionLocalOf<CyclewiseExtraColors> {
    error("No CyclewiseExtraColors provided")
}

val MaterialTheme.cycleColors: CyclewiseExtraColors
    @Composable @ReadOnlyComposable
    get() = LocalCyclewiseColors.current

@Composable
fun CyclewiseTheme(
    seedHue: Int   = 300,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = colorSchemeForHue(seedHue, darkTheme)

    val extraColors = CyclewiseExtraColors(
        status           = colorScheme.error,
        statusContainer  = colorScheme.errorContainer,
        onStatusContainer = colorScheme.onErrorContainer,
        seedHue          = seedHue,
        dark             = darkTheme,
    )

    CompositionLocalProvider(LocalCyclewiseColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = CyclewiseTypography,
            shapes      = CyclewiseShapes,
            content     = content,
        )
    }
}
