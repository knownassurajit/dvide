package com.knownassurajit.dvide_finance.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.DashHeader
import com.knownassurajit.dvide_finance.app.ui.dashboard.variants.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Icon
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons

@Composable
fun DashboardScreen(
    metrics: Metrics?,
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
    onAddCycle: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()

    // The entire dashboard (header + content) scrolls together
    Column(
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
            onAddCycle = onAddCycle,
            onOpenArchive = onOpenArchive
        )

        if (metrics == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = CwIcons.Plus,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No Active Cycle",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "You don't have an active cycle for today. Tap the + icon above to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
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
                        onAddTransaction = onAddTransaction,
                    )
                    DashboardVariant.GAUGE -> GaugeDashboard(
                        metrics     = metrics,
                        viewIsWeekly = viewIsWeekly,
                        onViewChange = onViewChange,
                        highlightId = highlightId,
                        onOpenCycle = onOpenCycle,
                        onDeleteTransaction = onDeleteTransaction,
                        onAddTransaction = onAddTransaction,
                    )
                    DashboardVariant.CARDS -> CardsDashboard(
                        metrics     = metrics,
                        viewIsWeekly = viewIsWeekly,
                        onViewChange = onViewChange,
                        highlightId = highlightId,
                        onOpenCycle = onOpenCycle,
                        onDeleteTransaction = onDeleteTransaction,
                        onAddTransaction = onAddTransaction,
                    )
                }
            }
        }
    }
}
