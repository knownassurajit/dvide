package com.dvide.app.ui.dashboard.variants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.dvide.app.data.model.Category
import com.dvide.app.domain.model.Metrics
import com.dvide.app.ui.components.*
import com.dvide.app.ui.theme.ShapeBucketCard
import com.dvide.app.ui.theme.cycleColors
import com.dvide.app.util.formatMoney
import com.dvide.app.util.moneyParts

// ════════════════════════════ C · CARDS ════════════════════════════
@Composable
fun CardsDashboard(
    metrics: Metrics,
    viewIsWeekly: Boolean,
    highlightId: Long?,
    onOpenCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cc    = MaterialTheme.cycleColors
    val sp    = metrics.safeToSpend.moneyParts()

    // Shape morph when tight
    val cornerTL by animateDpAsState(
        targetValue   = if (metrics.tight) 14.dp else 36.dp,
        animationSpec = spring(stiffness = 200f),
        label         = "cardsTL",
    )
    val cardShape = RoundedCornerShape(
        topStart   = cornerTL,  topEnd    = cornerTL,
        bottomEnd  = cornerTL,  bottomStart = 14.dp,
    )

    Column(modifier = modifier) {
        // Hero card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .clip(cardShape)
                .clickable(role = Role.Button, onClickLabel = "View cycle detail") { onOpenCycle() },
            shape = cardShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                // Chevron
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector        = CwIcons.ChevronRight,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier           = Modifier.align(Alignment.TopEnd),
                    )
                }

                Text(
                    text  = if (metrics.ended) {
                        if (metrics.balance >= 0) "CLOSED · SURPLUS" else "CLOSED · BORROWED"
                    } else "SAFE TO SPEND · PER DAY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.18f, androidx.compose.ui.unit.TextUnitType.Em),
                )

                Text(
                    text  = if (metrics.ended)
                                kotlin.math.abs(metrics.balance).formatMoney()
                            else "£${sp.whole}.${sp.frac}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(4.dp))

                AllocationBar(
                    metrics = metrics,
                    height  = 12.dp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom,
                ) {
                    Text(
                        text  = metrics.cycle.label(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.12f, androidx.compose.ui.unit.TextUnitType.Em),
                    )
                    Text(
                        text  = "${kotlin.math.abs(metrics.balance).formatMoney()} ${if (metrics.balance >= 0) "left" else "over"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Bucket stat cards
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BucketCard(
                label  = "Income",
                value  = metrics.income.formatMoney(),
                dotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            BucketCard(
                label  = "Set aside",
                value  = metrics.allocated.formatMoney(),
                dotColor = MaterialTheme.cycleColors.categoryColor("savings"),
                modifier = Modifier.weight(1f),
            )
            BucketCard(
                label  = "Spent",
                value  = metrics.spent.formatMoney(),
                dotColor = MaterialTheme.cycleColors.categoryColor("lifestyle"),
                modifier = Modifier.weight(1f),
            )
        }

        // Cycle progress bar
        CycleProgressBar(
            cycle    = metrics.cycle,
            tight    = metrics.tight,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )

        // Transaction timeline (compact)
        TransactionTimeline(
            metrics     = metrics,
            groupByWeek = viewIsWeekly,
            highlightId = highlightId,
            compact     = true,
            modifier    = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun BucketCard(
    label: String,
    value: String,
    dotColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape    = ShapeBucketCard,
        color    = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 13.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Text(
                    text  = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.1f, androidx.compose.ui.unit.TextUnitType.Em),
                    maxLines = 1,
                )
            }
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
