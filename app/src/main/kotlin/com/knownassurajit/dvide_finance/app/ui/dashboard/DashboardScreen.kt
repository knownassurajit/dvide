package com.knownassurajit.dvide_finance.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.DashHeader
import com.knownassurajit.dvide_finance.app.ui.dashboard.variants.CardsDashboard
import com.knownassurajit.dvide_finance.app.ui.dashboard.variants.EditorialDashboard
import com.knownassurajit.dvide_finance.app.ui.dashboard.variants.GaugeDashboard
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import com.knownassurajit.dvide_finance.app.ui.theme.dvideBottomBars

@Composable
fun DashboardScreen(
    metrics: Metrics?,
    variant: DashboardVariant,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    userName: String,
    highlightId: Long?,
    onOpenSettings: () -> Unit,
    onOpenCycle: () -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onAddCycle: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        DashHeader(
            userName       = userName,
            viewIsWeekly   = viewIsWeekly,
            onViewChange   = onViewChange,
            onOpenSettings = onOpenSettings,
            onOpenProfile  = onOpenProfile,
            onAddCycle     = onAddCycle,
            onOpenArchive  = onOpenArchive,
        )

        if (metrics == null) {
            EmptyCycleState(
                onAddCycle = onAddCycle,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                AnimatedContent(
                    targetState = variant,
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
}

@Composable
private fun EmptyCycleState(
    onAddCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = DvideDimens.screen)
            .dvideBottomBars()
            .padding(bottom = DvideDimens.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = CwIcons.Wallet,
            contentDescription = null,
            modifier = Modifier.size(DvideDimens.commit),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(DvideDimens.section))
        Text(
            text = "Start your first cycle",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DvideDimens.tight))
        Text(
            text = "DVIDE splits each pay window into set-aside, spendable, and a daily allowance. Create a cycle to begin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DvideDimens.section))
        Button(
            onClick = onAddCycle,
            modifier = Modifier
                .fillMaxWidth()
                .height(DvideDimens.commit)
                .testTag("empty_create_cycle"),
            shape = ShapeCommitBtn,
        ) {
            Icon(CwIcons.Plus, contentDescription = null)
            Spacer(modifier = Modifier.size(DvideDimens.tight))
            Text("Create cycle", fontWeight = FontWeight.SemiBold)
        }
    }
}
