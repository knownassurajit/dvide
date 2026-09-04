package com.knownassurajit.dvide_finance.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.HueSlider
import com.knownassurajit.dvide_finance.app.ui.components.PaydayCalendar
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup

private object SettingsSpace {
    val section = 32.dp
    val screenVertical = 20.dp
    val rowH = 16.dp
    val rowV = 16.dp
    val switchV = 12.dp
    val groupTitleBottom = 12.dp
    val inner = 12.dp
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onClose: () -> Unit,
    onToggleTheme: () -> Unit,
    onVariantChange: (DashboardVariant) -> Unit,
    onSeedHueChange: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onCurrencyChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onWeekStartChange: (Int) -> Unit,
    onNumberFormatChange: (String) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit = { onToggleTheme() },
    onDynamicColorChange: (Boolean) -> Unit = {},
    onPaydayChange: (Int) -> Unit = {},
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Settings",
                        style = MaterialTheme.typography.titleLarge,
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
        contentWindowInsets = WindowInsets.safeContent.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        modifier       = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DvideDimens.screenTight, vertical = SettingsSpace.screenVertical),
            verticalArrangement = Arrangement.spacedBy(SettingsSpace.section),
        ) {
            // Personal & Profile
            SettingsSection(title = "Profile") {
                SettingsRow(
                    label = "Account Details",
                    value = settings.userName.ifEmpty { "Not set" },
                    onClick = onOpenProfile,
                )
            }

            SettingsSection(title = "Appearance") {
                SettingsSwitchRow(
                    label = "Dark theme",
                    checked = settings.darkTheme,
                    onCheckedChange = onDarkThemeChange,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsSwitchRow(
                    label = "Material You",
                    checked = settings.dynamicColor,
                    onCheckedChange = onDynamicColorChange,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Column(modifier = Modifier.padding(SettingsSpace.inner)) {
                    Text(
                        text = "Dashboard layout",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(DvideDimens.item))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(DvideDimens.tight),
                        verticalArrangement = Arrangement.spacedBy(DvideDimens.tight),
                    ) {
                        DashboardVariant.entries.forEach { variant ->
                            FilterChip(
                                selected = settings.dashboardVariant == variant,
                                onClick = { onVariantChange(variant) },
                                label = { Text(variant.label) },
                            )
                        }
                    }
                }
                if (!settings.dynamicColor) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Column(modifier = Modifier.padding(SettingsSpace.inner)) {
                        Text(
                            text = "Brand colour",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(SettingsSpace.rowV))
                        HueSlider(
                            hue = settings.seedHue,
                            onHueChange = { onSeedHueChange(it.toInt()) },
                        )
                    }
                }
            }

            SettingsSection(title = "Pay cycle") {
                Column(
                    modifier = Modifier.padding(SettingsSpace.inner),
                    verticalArrangement = Arrangement.spacedBy(SettingsSpace.rowV),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Payday",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Day ${settings.payday}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    PaydayCalendar(
                        payday = settings.payday,
                        onPaydayChange = onPaydayChange,
                        weekStartDay = settings.weekStartDay,
                    )
                    Text(
                        text = "Used when suggesting the next cycle window. Days 29–31 stay available for longer months.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Localization
            SettingsSection(title = "Localization") {
                val currencySymbol = try {
                    java.util.Currency.getInstance(settings.currencyCode).getSymbol(java.util.Locale("", settings.regionCode))
                } catch (e: Exception) {
                    settings.currencyCode
                }
                val regionLabel = try {
                    java.util.Locale("", settings.regionCode).displayCountry
                } catch (e: Exception) {
                    settings.regionCode
                }
                val weekStartLabel = when (settings.weekStartDay) {
                    java.util.Calendar.SATURDAY -> "Saturday"
                    java.util.Calendar.SUNDAY -> "Sunday"
                    else -> "Monday"
                }
                val numberFormatLabel = when (settings.numberFormat) {
                    "DOT_DECIMAL" -> "1,234.56"
                    "COMMA_DECIMAL" -> "1.234,56"
                    "SPACE_DECIMAL" -> "1 234,56"
                    else -> "Default for Region"
                }

                SettingsRow(
                    label = "Currency",
                    value = "${settings.currencyCode} · $currencySymbol",
                    onClick = { showCurrencyDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Region",
                    value = regionLabel,
                    onClick = { showRegionDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Week starts on",
                    value = weekStartLabel,
                    onClick = { showWeekStartDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Number format",
                    value = numberFormatLabel,
                    onClick = { showNumberFormatDialog = true },
                )
            }

            SettingsSection(title = "Data") {
                SettingsRow(
                    label = "Export ledger",
                    value = "CSV",
                    onClick = onExport,
                )
            }

            Text(
                text = "dv/de · divide each cycle",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp),
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectDialog(
            current = settings.currencyCode,
            onSelect = onCurrencyChange,
            onDismiss = { showCurrencyDialog = false }
        )
    }
    if (showRegionDialog) {
        RegionSelectDialog(
            current = settings.regionCode,
            onSelect = onRegionChange,
            onDismiss = { showRegionDialog = false }
        )
    }
    if (showWeekStartDialog) {
        WeekStartSelectDialog(
            current = settings.weekStartDay,
            onSelect = onWeekStartChange,
            onDismiss = { showWeekStartDialog = false }
        )
    }
    if (showNumberFormatDialog) {
        NumberFormatSelectDialog(
            current = settings.numberFormat,
            onSelect = onNumberFormatChange,
            onDismiss = { showNumberFormatDialog = false }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text  = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = SettingsSpace.rowH, bottom = SettingsSpace.groupTitleBottom)
        )
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
}

@Composable
fun SettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = SettingsSpace.rowH, vertical = SettingsSpace.rowV),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = CwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsSpace.rowH, vertical = SettingsSpace.switchV),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun CurrencySelectDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val allCurrencies = remember {
        java.util.Currency.getAvailableCurrencies()
            .toList()
            .sortedBy { it.currencyCode }
    }
    val filtered = remember(search) {
        allCurrencies.filter {
            it.currencyCode.contains(search, ignoreCase = true) ||
            it.displayName.contains(search, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search currency...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Box(modifier = Modifier.height(300.dp)) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(filtered.size) { index ->
                            val c = filtered[index]
                            val label = "${c.currencyCode} - ${c.displayName}"
                            Surface(
                                onClick = {
                                    onSelect(c.currencyCode)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (c.currencyCode == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (c.currencyCode == current) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
             TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RegionSelectDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val allRegions = remember {
        java.util.Locale.getAvailableLocales()
            .filter { it.country.isNotEmpty() }
            .distinctBy { it.country }
            .sortedBy { it.displayCountry }
    }
    val filtered = remember(search) {
        allRegions.filter {
            it.country.contains(search, ignoreCase = true) ||
            it.displayCountry.contains(search, ignoreCase = true)
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Region") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search region...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Box(modifier = Modifier.height(300.dp)) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(filtered.size) { index ->
                            val r = filtered[index]
                            val label = "${r.displayCountry} (${r.country})"
                            Surface(
                                onClick = {
                                    onSelect(r.country)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (r.country == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (r.country == current) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun WeekStartSelectDialog(
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        java.util.Calendar.SATURDAY to "Saturday",
        java.util.Calendar.SUNDAY to "Sunday",
        java.util.Calendar.MONDAY to "Monday"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Week Starts On") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Surface(
                        onClick = {
                            onSelect(value)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (value == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (value == current) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun NumberFormatSelectDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "DEFAULT" to "Default for Region",
        "DOT_DECIMAL" to "1,234.56",
        "COMMA_DECIMAL" to "1.234,56",
        "SPACE_DECIMAL" to "1 234,56"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Number Format") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Surface(
                        onClick = {
                            onSelect(value)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (value == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (value == current) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
