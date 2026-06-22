package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeTimelineRow
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import java.time.LocalDate

@Composable
fun TransactionTimeline(
    metrics: Metrics,
    groupByWeek: Boolean,
    highlightId: Long?,
    compact: Boolean = false,
    onDeleteTransaction: ((Transaction) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today  = LocalDate.now()
    val formatter = LocalCurrencyFormatter.current
    val groups = if (groupByWeek)
        CycleEngine.groupByWeek(metrics.transactions, formatter.weekStartDay)
    else
        CycleEngine.groupByDay(metrics.transactions, today)

    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = modifier) {
        groups.forEach { group ->
            // Date divider
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically,
            ) {
                Text(
                    text       = group.label,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.16f, androidx.compose.ui.unit.TextUnitType.Em),
                )
                Text(
                    text  = formatter.format(group.total, 2),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Transaction rows
            group.items.forEach { tx ->
                val isNew    = tx.id == highlightId
                val catKind  = Category.kindOf(tx.category, tx.kind)
                val catColor = MaterialTheme.dvideColors.categoryColor(tx.category)

                AnimatedVisibility(
                    visible     = true,
                    enter       = if (isNew) scaleIn(spring(stiffness = 300f, dampingRatio = 0.6f)) + fadeIn()
                                  else       EnterTransition.None,
                    modifier    = Modifier.padding(bottom = 7.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ShapeTimelineRow)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .combinedClickable(
                                onLongClick = {
                                    if (onDeleteTransaction != null) {
                                        transactionToDelete = tx
                                    }
                                },
                                onClick = {}
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical   = if (compact) 10.dp else 13.dp,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Category dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(catColor),
                        )

                        // Note + category label
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = tx.note,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Text(
                                text  = Category.labelOf(tx.category).uppercase() +
                                        if (catKind == Category.Kind.ASIDE) " · SET ASIDE" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = androidx.compose.ui.unit.TextUnit(0.1f, androidx.compose.ui.unit.TextUnitType.Em),
                            )
                        }

                        // Amount
                        Text(
                            text  = if (catKind == Category.Kind.ASIDE) "↓ ${formatter.format(tx.amount, 2)}"
                                    else formatter.format(tx.amount, 2),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (catKind == Category.Kind.ASIDE)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete entry") },
            text = { Text("Are you sure you want to delete this transaction for ${formatter.format(tx.amount, 2)} (${tx.note})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction?.invoke(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
