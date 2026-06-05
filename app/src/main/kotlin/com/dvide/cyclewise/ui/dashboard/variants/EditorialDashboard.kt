package com.dvide.app.ui.dashboard.variants

import androidx.compose.foundation.clickable
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
import com.dvide.app.domain.model.Metrics
import com.dvide.app.ui.components.AllocationBar
import com.dvide.app.ui.components.CycleProgressBar
import com.dvide.app.ui.components.TransactionTimeline
import com.dvide.app.ui.theme.cycleColors
import com.dvide.app.util.formatMoney
import com.dvide.app.util.moneyParts

// ════════════════════════════ A · EDITORIAL ════════════════════════════
// Large editorial number with the spendable waterfall sub-row.
@Composable
fun EditorialDashboard(
    metrics: Metrics,
    viewIsWeekly: Boolean,
    highlightId: Long?,
    onOpenCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Hero number — tappable to open cycle detail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClickLabel = "View cycle detail") { onOpenCycle() }
                .padding(horizontal = 24.dp, vertical = 18.dp),
        ) {
            val statusColor = MaterialTheme.cycleColors.status

            Column {
                if (metrics.ended) {
                    Text(
                        text  = if (metrics.balance >= 0) "CLOSED WITH SURPLUS" else "CLOSED · BORROWED",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                    Text(
                        text  = kotlin.math.abs(metrics.balance).formatMoney(),
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
                    val sp = metrics.safeToSpend.moneyParts()
                    Text(
                        text  = "SAFE TO SPEND",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                    // Big annotated number: £whole.frac
                    Text(
                        text  = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold)) { append("£") }
                            append(sp.whole)
                            withStyle(SpanStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))) {
                                append(".${sp.frac}")
                            }
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (metrics.tight) statusColor
                                    else MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Text(
                        text  = "PER DAY · FOR ${metrics.cycle.remaining} DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                }
            }

            // Chevron affordance
            Icon(
                imageVector        = com.dvide.app.ui.components.CwIcons.ChevronRight,
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
                    text       = metrics.spendable.formatMoney(),
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
                    text       = kotlin.math.abs(metrics.balance).formatMoney(),
                    style      = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color      = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.cycleColors.status,
                )
            }
        }

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
            modifier     = Modifier.padding(horizontal = 20.dp),
        )
    }
}
