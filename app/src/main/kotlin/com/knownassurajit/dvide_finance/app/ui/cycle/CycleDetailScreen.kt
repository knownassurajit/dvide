package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.domain.model.PastCycle
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.util.ordinal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleDetailScreen(
    metrics: Metrics?,
    archive: List<PastCycle>,
    onClose: () -> Unit,
) {
    val formatter = LocalCurrencyFormatter.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cycle metrics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(CwIcons.Back, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        if (metrics == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                 Text(
                    text = "No Active Cycle",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                // ── Parameters ──
                SettingsLabel("Parameters")
                SettingsGroup {
                    CycleRow(
                        label    = "Cycle ends",
                        subLabel = "${metrics.cycle.remaining} days remaining",
                        trailing = metrics.cycle.end.dayOfMonth.ordinal(),
                    )
                    SettingsDivider()
                    CycleRow(
                        label    = "Baseline Income",
                        subLabel = "Fixed deposit",
                        trailing = formatter.format(metrics.income),
                    )
                }

                // ── Waterfall (Spendable calculation) ──
                SettingsLabel("Spendable")
                SettingsGroup {
                    WaterfallRow(
                        label = "Income",
                        op    = "+",
                        value = metrics.income,
                    )
                    if (metrics.allocated > 0) {
                        WaterfallRow(
                            label = "Set aside",
                            op    = "−",
                            value = metrics.allocated,
                            subLabel = "Excluded from pool",
                        )
                    }
                    WaterfallRow(
                        label  = "Spendable",
                        op     = "=",
                        value  = metrics.spendable,
                        strong = true,
                        rule   = true,
                    )
                }

                // ── Velocity & projections ──
                SettingsLabel("Projection")
                SettingsGroup {
                    val daysPassed = metrics.cycle.dayIndex + 1
                    WaterfallRow(
                        label    = "Daily velocity",
                        op       = "Ø",
                        value    = metrics.dailyVelocity,
                        subLabel = "${formatter.format(metrics.spent)} spent / $daysPassed days",
                    )
                    WaterfallRow(
                        label    = "Projected spend",
                        op       = "~",
                        value    = metrics.projectedSpend,
                        subLabel = "${formatter.format(metrics.dailyVelocity)} × ${metrics.cycle.totalDays} total days",
                    )
                    val projColor = if (metrics.projectedClose >= 0) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.dvideColors.status
                    WaterfallRow(
                        label      = "Projected close",
                        op         = "→",
                        value      = kotlin.math.abs(metrics.projectedClose),
                        strong     = true,
                        rule       = true,
                        valueColor = projColor,
                        subLabel   = if (metrics.projectedClose >= 0) "Expected surplus" else "Expected deficit",
                    )
                }

                // ── Aside breakdown ──
                val asideCategories = metrics.byCategory.entries
                    .filter { (cat, _) ->
                        Category.kindOf(cat, metrics.transactions.firstOrNull { it.category == cat }?.kind) == Category.Kind.ASIDE
                    }
                    .filter { it.value > 0 }

                if (asideCategories.isNotEmpty()) {
                    SettingsLabel("Set Aside · ${formatter.format(metrics.allocated)}")
                    Surface(
                        shape    = ShapeSettingsGroup,
                        color    = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            asideCategories.forEach { (cat, value) ->
                                CategoryBreakdownRow(
                                    categoryKey = cat,
                                    value       = value,
                                    denominator = metrics.allocated,
                                )
                            }
                        }
                    }
                }

                // ── Spending breakdown ──
                val expenseCategories = metrics.byCategory.entries
                    .filter { (cat, _) ->
                        Category.kindOf(cat, metrics.transactions.firstOrNull { it.category == cat }?.kind) == Category.Kind.EXPENSE
                    }
                    .filter { it.value > 0 }

                if (expenseCategories.isNotEmpty()) {
                    SettingsLabel("Spent · ${formatter.format(metrics.spent)}")
                    Surface(
                        shape    = ShapeSettingsGroup,
                        color    = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            expenseCategories.forEach { (cat, value) ->
                                CategoryBreakdownRow(
                                    categoryKey = cat,
                                    value       = value,
                                    denominator = metrics.spent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared sub-composables ──
@Composable
fun SettingsLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = ShapeSettingsGroup,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}

@Composable
private fun CycleRow(
    label:    String,
    subLabel: String?  = null,
    trailing: String?  = null,
    chevron:  Boolean  = false,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subLabel != null) {
                Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (chevron) {
            Icon(
                imageVector = CwIcons.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun WaterfallRow(
    label:      String,
    value:      Double,
    op:         String,
    subLabel:   String?  = null,
    strong:     Boolean  = false,
    rule:       Boolean  = false,
    valueColor: Color?   = null,
) {
    val textColor     = if (op == "−") MaterialTheme.colorScheme.onSurfaceVariant
                        else            MaterialTheme.colorScheme.onSurface
    val resolvedColor = valueColor ?: textColor

    Column {
        if (rule) {
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 18.dp),
                thickness = 1.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant,
            )
        }
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    if (op.isNotEmpty()) {
                        Text(
                            text  = op,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(16.dp),
                        )
                    }
                    Text(
                        text  = label,
                        style = if (strong)
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                        else
                            MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                }
                if (subLabel != null) {
                    Text(
                        text     = subLabel,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = if (op.isNotEmpty()) 16.dp else 0.dp),
                    )
                }
            }
            Text(
                text  = "${if (op == "−") "− " else ""}${LocalCurrencyFormatter.current.format(value)}",
                style = if (strong)
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                else
                    MaterialTheme.typography.bodyLarge,
                color = resolvedColor,
            )
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    categoryKey: String,
    value:       Double,
    denominator: Double,
) {
    val pct by animateFloatAsState(
        targetValue   = if (denominator > 0) (value / denominator).toFloat().coerceIn(0.002f, 1f) else 0.002f,
        animationSpec = spring(stiffness = 200f),
        label         = "catBreakdown",
    )
    val color = MaterialTheme.dvideColors.categoryColor(categoryKey)

    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color),
            )
            Text(
                text     = Category.labelOf(categoryKey),
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text  = LocalCurrencyFormatter.current.format(value),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(color),
            )
        }
    }
}

@Composable
fun BalanceLabel(balance: Double) {
    val positive = balance >= 0
    val cc       = MaterialTheme.dvideColors
    Text(
        text  = "${if (positive) "+" else "−"}${LocalCurrencyFormatter.current.format(kotlin.math.abs(balance))}",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = if (positive) MaterialTheme.colorScheme.onSurface else cc.status,
    )
}
