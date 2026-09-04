package com.knownassurajit.dvide_finance.app.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Bottom inset that clears the nav bar, gesture pill, and IME.
 *
 * Prefer [asPaddingValues] (does not consume) on FABs so nested Scaffolds
 * still see the same insets. Use [dvideBottomBars] on leaf columns.
 */
val DvideBottomInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeContent.only(WindowInsetsSides.Bottom)

@Composable
fun dvideBottomBarPadding(): Dp =
    DvideBottomInsets.asPaddingValues().calculateBottomPadding()

@Composable
fun Modifier.dvideBottomBars(): Modifier =
    windowInsetsPadding(DvideBottomInsets)

@Composable
fun BottomClearance(fab: Boolean = false) {
    val extra = if (fab) DvideDimens.fabClearance else DvideDimens.section
    Spacer(modifier = Modifier.height(extra + dvideBottomBarPadding()))
}
