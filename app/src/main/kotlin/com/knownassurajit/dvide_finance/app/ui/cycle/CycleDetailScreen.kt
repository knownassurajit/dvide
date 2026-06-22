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
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsDivider
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsGroup
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsLabel
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.util.ordinal

@Composable
fun CycleDetailScreen(
    metrics: Metrics,
    income: Double,
    anchorDay: Int,
    archive: List<PastCycle>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cc      = MaterialTheme.dvideColors
    val formatter = LocalCurrencyFormatter.current
    val ended   = metrics.ended
    val surplus = metrics.balance >= 0

    Surface(
        modifier = modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Screen header
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector        = CwIcons.Back,
                        contentDescription = "Back",
                        tint               = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text  = if (ended) "Cycle closed" else "This cycle",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
            ) {

                // ── Hero summary ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = if (ended)
                        if (surplus) MaterialTheme.colorScheme.primaryContainer
                        else cc.statusContainer
                    else MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        Text(
                            text  = metrics.cycle.label(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.16f, androidx.compose.ui.unit.TextUnitType.Em),
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text  = "${if (ended && surplus) "+" else if (ended) "−" else ""}${formatter.format(kotlin.math.abs(metrics.balance))}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    color = if (ended && !surplus) cc.status else MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier.alignByBaseline(),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text  = when {
                                    ended && surplus -> "surplus"
                                    ended            -> "borrowed"
                                    else             -> "left to spend"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                modifier = Modifier.alignByBaseline(),
                            )
                        }
                        Text(
                            text  = if (ended)
                                "${metrics.cycle.totalDays} days · settled"
                            else
                                "${metrics.cycle.dayIndex + 1} of ${metrics.cycle.totalDays} days · ${metrics.cycle.remaining} remaining",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Configuration ──
                SettingsLabel("Configuration")
                SettingsGroup {
                    CycleRow(
                        label    = "Cycle anchor",
                        subLabel = "Salary lands this day each month",
                        trailing = anchorDay.ordinal(),
                        chevron  = true,
                    )
                    SettingsDivider()
                    CycleRow(label = "Monthly income",  trailing = formatter.format(income), chevron = true)
                    SettingsDivider()
                    CycleRow(label = "Cycle window",    trailing = metrics.cycle.label())
                }

                // ── The waterfall ──
                SettingsLabel("The waterfall")
                SettingsGroup {
                    WaterfallRow(label = "Income",    value = metrics.income,    op = "")
                    SettingsDivider()
                    WaterfallRow(
                        label    = "Set aside", value = metrics.allocated, op = "−",
                        subLabel = "Savings · Investment · Security",
                    )
                    SettingsDivider()
                    WaterfallRow(label = "Spendable", value = metrics.spendable, op = "=", strong = true, rule = true)
                    SettingsDivider()
                    WaterfallRow(
                        label    = "Spent", value = metrics.spent, op = "−",
                        subLabel = "Essentials · Lifestyle",
                    )
                    SettingsDivider()
                    WaterfallRow(
                        label  = if (ended) if (surplus) "Surplus" else "Borrowed" else "Balance",
                        value  = kotlin.math.abs(metrics.balance),
                        op     = "=",
                        strong = true,
                        rule   = true,
                        valueColor = if (!surplus) MaterialTheme.dvideColors.status else null,
                    )
                }

                // ── Outlook (active cycle only) ──
                if (!ended) {
                    SettingsLabel("Outlook")
                    SettingsGroup {
                        Surface(
                            color   = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Projected close", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "At your current spending pace",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                BalanceLabel(metrics.projectedClose)
                            }
                        }
                        SettingsDivider()
                        CycleRow(
                            label    = "Safe to spend",
                            subLabel = "Per day · for ${metrics.cycle.remaining} days",
                            trailing = formatter.format(metrics.safeToSpend, 2),
                        )
                    }
                }

                // ── Set aside breakdown ──
                val asideCategories = metrics.byCategory.entries
                    .filter { (cat, _) ->
                        Category.kindOf(cat, metrics.transactions.firstOrNull { it.category == cat }?.kind) == Category.Kind.ASIDE
                    }
                    .filter { it.value > 0 }

                if (asideCategories.isNotEmpty()) {
                    SettingsLabel("Set aside · ${formatter.format(metrics.allocated)}")
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

                // ── Past cycles archive ──
                if (archive.isNotEmpty()) {
                    SettingsLabel("Past cycles")
                    SettingsGroup {
                        archive.forEachIndexed { index, pastCycle ->
                            if (index > 0) SettingsDivider()
                            Surface(
                                onClick = {},
                                color   = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier          = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pastCycle.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                        Text(pastCycle.range, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    BalanceLabel(pastCycle.balance)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector        = CwIcons.ChevronRight,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
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
private fun BalanceLabel(balance: Double) {
    val positive = balance >= 0
    val cc       = MaterialTheme.dvideColors
    Text(
        text  = "${if (positive) "+" else "−"}${LocalCurrencyFormatter.current.format(kotlin.math.abs(balance))}",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = if (positive) MaterialTheme.colorScheme.onSurface else cc.status,
    )
}
