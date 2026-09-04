package com.knownassurajit.dvide_finance.app.ui.dashboard.variants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeBucketCard
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors

@Composable
fun CardsDashboard(
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
    val safeAmount = if (viewIsWeekly) metrics.safeToSpend * 7.0 else metrics.safeToSpend
    val sp = formatter.parts(safeAmount)

    val cornerTL by animateDpAsState(
        targetValue = if (metrics.tight) 14.dp else 36.dp,
        animationSpec = spring(stiffness = 200f),
        label = "cardsTL",
    )
    val cardShape = RoundedCornerShape(
        topStart = cornerTL, topEnd = cornerTL,
        bottomEnd = cornerTL, bottomStart = 14.dp,
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
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = DvideDimens.card,
                            end = DvideDimens.card,
                            top = DvideDimens.card,
                            bottom = DvideDimens.item,
                        ),
                    verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
                ) {
                    Text(
                        text = if (metrics.ended) {
                            if (metrics.balance >= 0) "Closed · surplus" else "Closed · borrowed"
                        } else if (viewIsWeekly) "Safe to spend · per week" else "Safe to spend · per day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = DvideDimens.touch / 2),
                    )

                    MoneyText(
                        text = if (metrics.ended)
                            formatter.format(kotlin.math.abs(metrics.balance))
                        else
                            "${sp.symbol}${sp.whole}${sp.decimalSeparator}${sp.frac}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(end = DvideDimens.touch / 2),
                        minSize = 22.sp,
                    )

                    AllocationBar(
                        metrics = metrics,
                        height = 12.dp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = metrics.cycle.label(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${formatter.format(kotlin.math.abs(metrics.balance))} ${if (metrics.balance >= 0) "left" else "over"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                        )
                    }
                }

                Icon(
                    imageVector = CwIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(DvideDimens.item),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen)
                .padding(bottom = DvideDimens.tight),
            horizontalArrangement = Arrangement.spacedBy(DvideDimens.item),
        ) {
            BucketCard(
                label = "Income",
                value = formatter.format(metrics.income),
                dotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            BucketCard(
                label = "Set aside",
                value = formatter.format(metrics.allocated),
                dotColor = MaterialTheme.dvideColors.categoryColor("savings"),
                modifier = Modifier.weight(1f),
            )
            BucketCard(
                label = "Spent",
                value = formatter.format(metrics.spent),
                dotColor = MaterialTheme.dvideColors.categoryColor("lifestyle"),
                modifier = Modifier.weight(1f),
            )
        }

        CycleProgressBar(
            cycle = metrics.cycle,
            tight = metrics.tight,
            modifier = Modifier.padding(horizontal = DvideDimens.screen, vertical = DvideDimens.item),
        )

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
private fun BucketCard(
    label: String,
    value: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = ShapeBucketCard,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = DvideDimens.item, vertical = DvideDimens.item),
            verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DvideDimens.hairline),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            MoneyText(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                minSize = 11.sp,
            )
        }
    }
}
