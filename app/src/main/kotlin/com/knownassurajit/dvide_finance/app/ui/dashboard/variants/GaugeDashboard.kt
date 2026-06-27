package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.*
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeGaugeCard
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeGaugeCardSharp
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors

// ════════════════════════════ B · GAUGE ════════════════════════════
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
    val cc    = MaterialTheme.dvideColors
    val formatter = LocalCurrencyFormatter.current
    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
    val sp    = formatter.parts(safeAmount)

    // Shape morph: asymmetric → squared corners when tight
    val cornerTL by animateDpAsState(
        targetValue   = if (metrics.tight) 16.dp else 40.dp,
        animationSpec = spring(stiffness = 200f),
        label         = "gaugeTL",
    )
    val cornerBR by animateDpAsState(
        targetValue   = if (metrics.tight) 16.dp else 40.dp,
        animationSpec = spring(stiffness = 200f),
        label         = "gaugeBR",
    )
    val cardShape = RoundedCornerShape(
        topStart    = cornerTL, topEnd    = cornerTL,
        bottomEnd   = cornerBR, bottomStart = 16.dp,
    )

    Column(modifier = modifier) {
        // Gauge card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .clip(cardShape)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = { onOpenCycle() },
                    onLongClick = { onViewChange(!viewIsWeekly) }
                ),
            shape = cardShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier            = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Chevron
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector        = CwIcons.ChevronRight,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.align(Alignment.TopEnd),
                    )
                }

                // Ring gauge
                RingGauge(
                    value       = metrics.balanceFraction,
                    size        = 236.dp,
                    strokeWidth = 20.dp,
                    fillColor   = if (metrics.tight) cc.status else MaterialTheme.colorScheme.primary,
                    trackColor  = MaterialTheme.colorScheme.surfaceContainerHigh,
                    sharp       = metrics.tight,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(20.dp),
                    ) {
                        Text(
                            text  = if (viewIsWeekly) "SAFE / WEEK" else "SAFE / DAY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.16f, androidx.compose.ui.unit.TextUnitType.Em),
                        )
                        Text(
                            text  = "${sp.symbol}${sp.whole}",
                            style = MaterialTheme.typography.displayMedium,
                            color = if (metrics.tight) cc.status else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text  = "${formatter.format(metrics.balance)} of ${formatter.format(metrics.spendable)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Legend rows
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    GaugeLegendRow(
                        dot   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        label = "Set aside",
                        value = formatter.format(metrics.allocated),
                    )
                    GaugeLegendRow(
                        dot   = MaterialTheme.dvideColors.categoryColor("essentials"),
                        label = "Spent",
                        value = formatter.format(metrics.spent),
                    )
                    GaugeLegendRow(
                        dotBorder = true,
                        label     = if (metrics.balance >= 0) "Balance" else "Over by",
                        value     = formatter.format(kotlin.math.abs(metrics.balance)),
                        valueColor = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface else cc.status,
                    )
                }
            }
        }

        // Transaction timeline (compact)
        TransactionTimeline(
            metrics     = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            compact     = true,
            onDeleteTransaction = onDeleteTransaction,
            onAddTransaction = onAddTransaction,
            modifier    = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun GaugeLegendRow(
    dot: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    dotBorder: Boolean = false,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .let {
                    if (dotBorder) it
                    else it.also {}
                }
        ) {
            if (!dotBorder) {
                Surface(modifier = Modifier.fillMaxSize(), color = dot, shape = CircleShape) {}
            } else {
                Surface(
                    modifier  = Modifier.fillMaxSize(),
                    color     = androidx.compose.ui.graphics.Color.Transparent,
                    shape     = CircleShape,
                    border    = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {}
            }
        }
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.weight(1f),
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.1f, androidx.compose.ui.unit.TextUnitType.Em),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = valueColor,
        )
    }
}
