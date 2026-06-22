package com.knownassurajit.dvide_finance.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.DashHeader
import com.knownassurajit.dvide_finance.app.ui.dashboard.variants.*

@Composable
fun DashboardScreen(
    metrics: Metrics,
    variant: DashboardVariant,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    userName: String,
    darkTheme: Boolean,
    highlightId: Long?,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCycle: () -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()

    // The entire dashboard (header + content) scrolls together
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scroll),
    ) {
        DashHeader(
            userName      = userName,
            viewIsWeekly  = viewIsWeekly,
            onViewChange  = onViewChange,
            darkTheme     = darkTheme,
            onToggleTheme = onToggleTheme,
            onOpenSettings = onOpenSettings,
            onOpenProfile  = onOpenProfile,
        )

        AnimatedContent(
            targetState   = variant,
            transitionSpec = {
                (fadeIn(spring(stiffness = 300f)) + scaleIn(spring(stiffness = 300f), initialScale = 0.97f))
                    .togetherWith(fadeOut())
            },
            label = "dashVariant",
        ) { currentVariant ->
            when (currentVariant) {
                DashboardVariant.EDITORIAL -> EditorialDashboard(
                    metrics     = metrics,
                    viewIsWeekly = viewIsWeekly,
                    onViewChange = onViewChange,
                    highlightId = highlightId,
                    onOpenCycle = onOpenCycle,
                    onDeleteTransaction = onDeleteTransaction,
                )
                DashboardVariant.GAUGE -> GaugeDashboard(
                    metrics     = metrics,
                    viewIsWeekly = viewIsWeekly,
                    onViewChange = onViewChange,
                    highlightId = highlightId,
                    onOpenCycle = onOpenCycle,
                    onDeleteTransaction = onDeleteTransaction,
                )
                DashboardVariant.CARDS -> CardsDashboard(
                    metrics     = metrics,
                    viewIsWeekly = viewIsWeekly,
                    onViewChange = onViewChange,
                    highlightId = highlightId,
                    onOpenCycle = onOpenCycle,
                    onDeleteTransaction = onDeleteTransaction,
                )
            }
        }
    }
}

