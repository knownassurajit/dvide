package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.components.AllocationBar
import com.knownassurajit.dvide_finance.app.ui.components.CycleProgressBar
import com.knownassurajit.dvide_finance.app.ui.components.MoneyText
import com.knownassurajit.dvide_finance.app.ui.components.TransactionTimeline
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "View cycle detail",
                    onClick = { onOpenCycle() },
                    onLongClick = { onViewChange(!viewIsWeekly) },
                )
                .padding(
                    start = DvideDimens.screen,
                    end = DvideDimens.screen,
                    top = DvideDimens.item,
                    bottom = DvideDimens.item,
                ),
        ) {
            val statusColor = MaterialTheme.dvideColors.status

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = DvideDimens.touch / 2),
                verticalArrangement = Arrangement.spacedBy(DvideDimens.hairline),
            ) {
                if (metrics.ended) {
                    Text(
                        text = if (metrics.balance >= 0) "Closed with surplus" else "Closed · borrowed",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyText(
                        text = formatter.format(kotlin.math.abs(metrics.balance)),
                        style = MaterialTheme.typography.displayLarge,
                        color = if (metrics.balance >= 0)
                            MaterialTheme.colorScheme.onSurface
                        else statusColor,
                        modifier = Modifier.fillMaxWidth(),
                        minSize = 28.sp,
                    )
                    Text(
                        text = metrics.cycle.label(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
                    val sp = formatter.parts(safeAmount)
                    Text(
                        text = if (viewIsWeekly) "Safe to spend · weekly" else "Safe to spend · daily",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyText(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)) {
                                append(sp.symbol)
                            }
                            append(sp.whole)
                            withStyle(
                                SpanStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                ),
                            ) {
                                append("${sp.decimalSeparator}${sp.frac}")
                            }
                        },
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (metrics.tight) statusColor
                            else MaterialTheme.colorScheme.onSurface,
                        ),
                        color = if (metrics.tight) statusColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = if (viewIsWeekly)
                            "Per week · ${metrics.cycle.remaining} days left"
                        else
                            "Per day · ${metrics.cycle.remaining} days left",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Icon(
                imageVector = com.knownassurajit.dvide_finance.app.ui.components.CwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        AllocationBar(
            metrics = metrics,
            height = 12.dp,
            modifier = Modifier.padding(horizontal = DvideDimens.screen, vertical = DvideDimens.tight),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item),
            horizontalArrangement = Arrangement.spacedBy(DvideDimens.item),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spendable",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoneyText(
                    text = formatter.format(metrics.spendable),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    minSize = 16.sp,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (metrics.balance >= 0) "Balance" else "Over by",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoneyText(
                    text = formatter.format(kotlin.math.abs(metrics.balance)),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (metrics.balance >= 0) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.dvideColors.status,
                    modifier = Modifier.fillMaxWidth(),
                    minSize = 16.sp,
                    textAlign = TextAlign.End,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = DvideDimens.screen, vertical = DvideDimens.tight),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        CycleProgressBar(
            cycle = metrics.cycle,
            tight = metrics.tight,
            modifier = Modifier.padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item),
        )

        TransactionTimeline(
            metrics = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            onDeleteTransaction = onDeleteTransaction,
            onAddTransaction = onAddTransaction,
            modifier = Modifier.padding(horizontal = DvideDimens.screen),
        )
    }
}
