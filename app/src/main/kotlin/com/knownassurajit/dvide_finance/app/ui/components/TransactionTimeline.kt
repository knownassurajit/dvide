package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.ui.theme.BottomClearance
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.M3XMotion
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeTimelineRow
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionTimeline(
    metrics: Metrics,
    groupByWeek: Boolean,
    highlightId: Long?,
    compact: Boolean = false,
    onDeleteTransaction: ((Transaction) -> Unit)? = null,
    onEditTransaction: ((Transaction) -> Unit)? = null,
    onAddTransaction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today  = LocalDate.now()
    val formatter = LocalCurrencyFormatter.current
    val groups = if (groupByWeek)
        CycleEngine.groupByWeek(metrics.transactions, formatter.weekStartDay)
    else
        CycleEngine.groupByDay(metrics.transactions, today)

    var showDeleteConfirm by remember { mutableStateOf<Transaction?>(null) }
    var showActionSheet by remember { mutableStateOf<Transaction?>(null) }
    var expandedTxId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(DvideDimens.tight)) {
        if (groups.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CwIcons.Wallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        text = "No entries in this cycle yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Tap + to log an expense or allocate savings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        groups.forEach { group ->
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = DvideDimens.tight, bottom = DvideDimens.hairline),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically,
            ) {
                Text(
                    text       = group.label,
                    style      = MaterialTheme.typography.labelLarge,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = formatter.format(group.total, 2),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Transaction rows
            group.items.forEach { tx ->
                val isNew      = tx.id == highlightId
                val isExpanded = tx.id == expandedTxId
                val catKind    = Category.kindOf(tx.category, tx.kind)
                val catColor   = tx.color?.let { Color(it) } ?: MaterialTheme.dvideColors.categoryColor(tx.category)

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                            showDeleteConfirm = tx
                            false
                        } else false
                    }
                )

                AnimatedVisibility(
                    visible     = true,
                    enter       = if (isNew) scaleIn(spring(stiffness = M3XMotion.StiffnessSpatial, dampingRatio = M3XMotion.DampingSpatial)) + fadeIn()
                                  else       EnterTransition.None,
                    modifier    = Modifier.padding(bottom = 7.dp),
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(ShapeTimelineRow)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Delete",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Icon(
                                        imageVector = CwIcons.Delete,
                                        contentDescription = "Delete entry",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ShapeTimelineRow)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .m3xPressEffect(targetScale = 0.99f)
                                .combinedClickable(
                                    onLongClick = {
                                        showActionSheet = tx
                                    },
                                    onClick = {
                                        expandedTxId = if (isExpanded) null else tx.id
                                    }
                                )
                                .padding(
                                    horizontal = DvideDimens.screenTight,
                                    vertical   = if (compact) 10.dp else 12.dp,
                                ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text  = Category.labelOf(tx.category) +
                                                if (catKind == Category.Kind.ASIDE) " · set aside"
                                                else if (tx.kind == "income") " · top-up" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                // Amount
                                Text(
                                    text  = if (catKind == Category.Kind.ASIDE) "↓ ${formatter.format(tx.amount, 2)}"
                                            else if (tx.kind == "income") "+ ${formatter.format(tx.amount, 2)}"
                                            else formatter.format(tx.amount, 2),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (catKind == Category.Kind.ASIDE)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else if (tx.kind == "income")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            // Expanded detail view with delete and edit icons
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(M3XMotion.spatial()) + fadeIn(),
                                exit = shrinkVertically(M3XMotion.spatial()) + fadeOut(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                ) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 0.5.dp,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${tx.date.dayOfWeek.name.take(3)}, ${tx.date.dayOfMonth} ${tx.date.month.name.take(3)} ${tx.date.year}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (onEditTransaction != null && tx.kind != "income") {
                                                TextButton(
                                                    onClick = {
                                                        onEditTransaction(tx)
                                                        expandedTxId = null
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                ) {
                                                    Icon(CwIcons.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Edit")
                                                }
                                            }
                                            if (onDeleteTransaction != null) {
                                                TextButton(
                                                    onClick = {
                                                        showDeleteConfirm = tx
                                                        expandedTxId = null
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.textButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error,
                                                    ),
                                                ) {
                                                    Icon(CwIcons.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Delete")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomClearance(fab = true)
    }

    // Long-press options dialog
    if (showActionSheet != null) {
        val tx = showActionSheet!!
        AlertDialog(
            onDismissRequest = { showActionSheet = null },
            title = { Text(tx.note) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${formatter.format(tx.amount, 2)} · ${Category.labelOf(tx.category)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (onEditTransaction != null && tx.kind != "income") {
                        Surface(
                            onClick = {
                                showActionSheet = null
                                onEditTransaction(tx)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(CwIcons.Edit, contentDescription = null)
                                Text("Edit entry", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    if (onDeleteTransaction != null) {
                        Surface(
                            onClick = {
                                showActionSheet = null
                                showDeleteConfirm = tx
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(CwIcons.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text("Delete entry", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showActionSheet = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirm != null) {
        val tx = showDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete entry") },
            text = { Text("Are you sure you want to delete this transaction for ${formatter.format(tx.amount, 2)} (${tx.note})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction?.invoke(tx)
                        showDeleteConfirm = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
