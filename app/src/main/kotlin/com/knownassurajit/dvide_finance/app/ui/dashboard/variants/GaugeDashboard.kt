package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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

@Composable
fun GaugeDashboard(
    metrics: Metrics,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    highlightId: Long?,
    onOpenCycle: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onAddTransaction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val cc = MaterialTheme.dvideColors
    val formatter = LocalCurrencyFormatter.current
    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
    val sp = formatter.parts(safeAmount)

    val cornerTL by animateDpAsState(
        targetValue = if (metrics.tight) 16.dp else 40.dp,
        animationSpec = spring(stiffness = 200f),
        label = "gaugeTL",
    )
    val cornerBR by animateDpAsState(
        targetValue = if (metrics.tight) 16.dp else 40.dp,
        animationSpec = spring(stiffness = 200f),
        label = "gaugeBR",
    )
    val cardShape = RoundedCornerShape(
        topStart = cornerTL, topEnd = cornerTL,
        bottomEnd = cornerBR, bottomStart = 16.dp,
    )

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item)
                .clip(cardShape)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = { onOpenCycle() },
                    onLongClick = { onViewChange(!viewIsWeekly) },
                ),
            shape = cardShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DvideDimens.card),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DvideDimens.item),
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val ring = maxWidth.coerceAtMost(240.dp).coerceAtLeast(168.dp)
                        val stroke = if (ring < 200.dp) 14.dp else 18.dp
                        RingGauge(
                            value = metrics.balanceFraction,
                            size = ring,
                            strokeWidth = stroke,
                            fillColor = if (metrics.tight) cc.status else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            sharp = metrics.tight,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = DvideDimens.item),
                            ) {
                                Text(
                                    text = if (viewIsWeekly) "Safe / week" else "Safe / day",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                MoneyText(
                                    text = "${sp.symbol}${sp.whole}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = if (metrics.tight) cc.status else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    minSize = 22.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "${formatter.format(metrics.balance)} of ${formatter.format(metrics.spendable)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
                    ) {
                        GaugeLegendRow(
                            dot = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            label = "Set aside",
                            value = formatter.format(metrics.allocated),
                        )
                        GaugeLegendRow(
                            dot = MaterialTheme.dvideColors.categoryColor("essentials"),
                            label = "Spent",
                            value = formatter.format(metrics.spent),
                        )
                        GaugeLegendRow(
                            dotBorder = true,
                            label = if (metrics.balance >= 0) "Balance" else "Over by",
                            value = formatter.format(kotlin.math.abs(metrics.balance)),
                            valueColor = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface else cc.status,
                        )
                    }
                }

                Icon(
                    imageVector = CwIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DvideDimens.item),
                )
            }
        }

        TransactionTimeline(
            metrics = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            compact = true,
            onDeleteTransaction = onDeleteTransaction,
            onAddTransaction = onAddTransaction,
            modifier = Modifier.padding(horizontal = DvideDimens.screen),
        )
    }
}

@Composable
private fun GaugeLegendRow(
    dot: Color = Color.Transparent,
    dotBorder: Boolean = false,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DvideDimens.tight),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape)) {
            if (!dotBorder) {
                Surface(modifier = Modifier.fillMaxSize(), color = dot, shape = CircleShape) {}
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {}
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        MoneyText(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            minSize = 12.sp,
        )
    }
}
