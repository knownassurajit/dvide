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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.MoneyText
import com.knownassurajit.dvide_finance.app.ui.components.RingGauge
import com.knownassurajit.dvide_finance.app.ui.components.TransactionTimeline
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect

@Composable
fun FocusDashboard(
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
    val cc = MaterialTheme.dvideColors
    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
    val sp = formatter.parts(safeAmount)

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Focus Card with Ring Gauge
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item)
                .clip(RoundedCornerShape(32.dp))
                .m3xPressEffect(targetScale = 0.99f)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = onOpenCycle,
                    onLongClick = { onViewChange(!viewIsWeekly) },
                ),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Header label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (metrics.tight) cc.status.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (metrics.tight) cc.status else MaterialTheme.colorScheme.primary),
                                )
                                Text(
                                    text = if (metrics.tight) "Tight Runway" else "On Track",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (metrics.tight) cc.status else MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "${metrics.cycle.remaining} days left",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = CwIcons.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Large Circular Focus Gauge
                    RingGauge(
                        value = metrics.balanceFraction,
                        size = 210.dp,
                        strokeWidth = 18.dp,
                        fillColor = if (metrics.tight) cc.status else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        sharp = metrics.tight,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = if (viewIsWeekly) "Weekly Quota" else "Daily Quota",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            MoneyText(
                                text = "${sp.symbol}${sp.whole}${if (sp.frac.isNotEmpty()) "${sp.decimalSeparator}${sp.frac}" else ""}",
                                style = MaterialTheme.typography.displayMedium,
                                color = if (metrics.tight) cc.status else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                minSize = 22.sp,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Safe to spend",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                    }

                    // Runway Details Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = "Cycle Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoneyText(
                                    text = formatter.format(metrics.balance),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface else cc.status,
                                    minSize = 13.sp,
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoneyText(
                                    text = formatter.format(metrics.spent),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    minSize = 13.sp,
                                )
                            }
                        }
                    }
                }
        }

        TransactionTimeline(
            metrics = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            compact = true,
            onDeleteTransaction = onDeleteTransaction,
            onEditTransaction = onEditTransaction,
            onAddTransaction = onAddTransaction,
            modifier = Modifier.padding(horizontal = DvideDimens.screen),
        )
    }
}
