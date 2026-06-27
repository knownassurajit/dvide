package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.AllocationBar
import com.knownassurajit.dvide_finance.app.ui.components.CycleProgressBar
import com.knownassurajit.dvide_finance.app.ui.components.TransactionTimeline
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors

// ════════════════════════════ A · EDITORIAL ════════════════════════════
// Large editorial number with the spendable waterfall sub-row.
@Composable
fun EditorialDashboard(
    metrics: Metrics,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    highlightId: Long?,
    onOpenCycle: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onAddTransaction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalCurrencyFormatter.current

    Column(modifier = modifier) {
        // Hero number — tappable to open cycle detail, long-press to toggle daily/weekly view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = { onOpenCycle() },
                    onLongClick = { onViewChange(!viewIsWeekly) }
                )
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            val statusColor = MaterialTheme.dvideColors.status

            Column {
                if (metrics.ended) {
                    Text(
                        text  = if (metrics.balance >= 0) "CLOSED WITH SURPLUS" else "CLOSED · BORROWED",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                    Text(
                        text  = formatter.format(kotlin.math.abs(metrics.balance)),
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (metrics.balance >= 0)
                                        MaterialTheme.colorScheme.onSurface
                                    else statusColor,
                        ),
                    )
                    Text(
                        text  = metrics.cycle.label(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                } else {
                    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
                    val sp = formatter.parts(safeAmount)
                    Text(
                        text  = if (viewIsWeekly) "SAFE TO SPEND (WEEKLY)" else "SAFE TO SPEND (DAILY)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                    // Big annotated number: symbol whole.frac
                    Text(
                        text  = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold)) { append(sp.symbol) }
                            append(sp.whole)
                            withStyle(SpanStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))) {
                                append("${sp.decimalSeparator}${sp.frac}")
                            }
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (metrics.tight) statusColor
                                    else MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Text(
                        text  = if (viewIsWeekly) "PER WEEK · FOR ${metrics.cycle.remaining} DAYS" else "PER DAY · FOR ${metrics.cycle.remaining} DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                }
            }

            // Chevron affordance
            Icon(
                imageVector        = com.knownassurajit.dvide_finance.app.ui.components.CwIcons.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.align(Alignment.TopEnd).padding(top = 4.dp),
            )
        }

        // Allocation bar
        AllocationBar(
            metrics  = metrics,
            height   = 14.dp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        )

        // Spendable / Balance sub-row
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text  = "SPENDABLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.14f, androidx.compose.ui.unit.TextUnitType.Em),
                )
                Text(
                    text       = formatter.format(metrics.spendable),
                    style      = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color      = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = if (metrics.balance >= 0) "BALANCE" else "OVER BY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.14f, androidx.compose.ui.unit.TextUnitType.Em),
                )
                Text(
                    text       = formatter.format(kotlin.math.abs(metrics.balance)),
                    style      = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color      = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.dvideColors.status,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            modifier  = Modifier.padding(horizontal = 24.dp),
            thickness = 1.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant,
        )

        // Cycle progress bar
        CycleProgressBar(
            cycle    = metrics.cycle,
            tight    = metrics.tight,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
        )

        // Transaction timeline
        TransactionTimeline(
            metrics      = metrics,
            groupByWeek  = viewIsWeekly,
            highlightId  = highlightId,
            onDeleteTransaction = onDeleteTransaction,
            onAddTransaction = onAddTransaction,
            modifier     = Modifier.padding(horizontal = 20.dp),
        )
    }
}
