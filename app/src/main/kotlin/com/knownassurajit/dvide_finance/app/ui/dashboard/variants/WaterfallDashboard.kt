package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.AllocationBar
import com.knownassurajit.dvide_finance.app.ui.components.CycleProgressBar
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.MoneyText
import com.knownassurajit.dvide_finance.app.ui.components.TransactionTimeline
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect

@Composable
fun WaterfallDashboard(
    metrics: Metrics,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    highlightId: Long?,
    onOpenCycle: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: ((Transaction) -> Unit)? = null,
    onAddTransaction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalCurrencyFormatter.current
    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
    val sp = formatter.parts(safeAmount)
    val cc = MaterialTheme.dvideColors

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Waterfall Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item)
                .clip(RoundedCornerShape(28.dp))
                .m3xPressEffect(targetScale = 0.99f)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = onOpenCycle,
                    onLongClick = { onViewChange(!viewIsWeekly) },
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DvideDimens.card),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (metrics.ended) {
                            if (metrics.balance >= 0) "Closed · Surplus" else "Closed · Overspent"
                        } else if (viewIsWeekly) "Safe to spend · Weekly quota" else "Safe to spend · Daily quota",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "${metrics.cycle.remaining}d left",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            imageVector = CwIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                MoneyText(
                    text = "${sp.symbol}${sp.whole}${if (sp.frac.isNotEmpty()) "${sp.decimalSeparator}${sp.frac}" else ""}",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (metrics.tight) cc.status else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    minSize = 24.sp,
                )

                AllocationBar(
                    metrics = metrics,
                    height = 10.dp,
                )

                Text(
                    text = "${formatter.format(metrics.balance)} left from ${formatter.format(metrics.spendable)} spendable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Waterfall Pillars Breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen)
                .padding(bottom = DvideDimens.item),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WaterfallStepRow(
                step = "1",
                label = "Total Income",
                amount = formatter.format(metrics.income),
                color = MaterialTheme.colorScheme.primary,
                subtext = "Initial cycle base & top-ups",
            )
            WaterfallStepRow(
                step = "2",
                label = "Set Aside",
                amount = "- ${formatter.format(metrics.allocated)}",
                color = cc.categoryColor("savings"),
                subtext = "Savings, bills & commitments",
            )
            WaterfallStepRow(
                step = "3",
                label = "Spendable Pool",
                amount = formatter.format(metrics.spendable),
                color = MaterialTheme.colorScheme.secondary,
                subtext = "Income minus set-aside",
            )
            WaterfallStepRow(
                step = "4",
                label = "Total Spent",
                amount = "- ${formatter.format(metrics.spent)}",
                color = if (metrics.tight) cc.status else cc.categoryColor("lifestyle"),
                subtext = "Expenses logged to date",
            )
        }

        CycleProgressBar(
            cycle = metrics.cycle,
            tight = metrics.tight,
            modifier = Modifier.padding(horizontal = DvideDimens.screen, vertical = DvideDimens.tight),
        )

        TransactionTimeline(
            metrics = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            onDeleteTransaction = onDeleteTransaction,
            onEditTransaction = onEditTransaction,
            onAddTransaction = onAddTransaction,
            modifier = Modifier.padding(horizontal = DvideDimens.screen),
        )
    }
}

@Composable
private fun WaterfallStepRow(
    step: String,
    label: String,
    amount: String,
    color: Color,
    subtext: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = step,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}
