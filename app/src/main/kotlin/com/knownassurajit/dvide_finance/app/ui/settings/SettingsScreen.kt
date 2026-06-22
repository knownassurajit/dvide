package com.knownassurajit.dvide_finance.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.ui.components.*
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup
import com.knownassurajit.dvide_finance.app.util.formatMoney

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
    onIncomeChange: (Double) -> Unit,
    onAnchorDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showAnchorDayDialog by remember { mutableStateOf(false) }

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
                    text  = "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Scrollable body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {

                // ── Display ──
                SettingsLabel("Display")
                SettingsGroup {
                    // Dashboard style selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text  = "Dashboard style",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        SegmentedControl(
                            options   = DashboardVariant.entries.map { it.label },
                            selected  = DashboardVariant.entries.indexOf(settings.dashboardVariant),
                            onSelect  = { onVariantChange(DashboardVariant.entries[it]) },
                            modifier  = Modifier.fillMaxWidth(),
                        )
                    }
                    SettingsDivider()

                    // Dark theme switch
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text     = "Dark theme",
                            style    = MaterialTheme.typography.bodyLarge,
                            color    = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked         = settings.darkTheme,
                            onCheckedChange = { onToggleTheme() },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor       = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor       = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor     = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor     = MaterialTheme.colorScheme.surface,
                                uncheckedBorderColor    = MaterialTheme.colorScheme.outline,
                            ),
                        )
                    }
                    SettingsDivider()

                    // App colour hue slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text     = "App colour",
                                style    = MaterialTheme.typography.bodyLarge,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            // Preview swatch
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        com.knownassurajit.dvide_finance.app.data.model.Category.oklchToColor(
                                            0.65f, 0.15f, settings.seedHue
                                        )
                                    ),
                            )
                        }
                        HueSlider(hue = settings.seedHue, onHueChange = onSeedHueChange)
                    }
                }

                // ── Region & currency ──
                SettingsLabel("Region & currency")
                SettingsGroup {
                    val currencySymbol = try {
                        java.util.Currency.getInstance(settings.currencyCode).getSymbol(java.util.Locale("", settings.regionCode))
                    } catch (e: Exception) {
                        "£"
                    }
                    val regionLabel = try {
                        java.util.Locale("", settings.regionCode).displayCountry
                    } catch (e: Exception) {
                        "United Kingdom"
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
                        trailing = "${settings.currencyCode} · $currencySymbol",
                        chevron = true,
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        label = "Region",
                        trailing = regionLabel,
                        chevron = true,
                        onClick = { showRegionDialog = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        label = "Week starts on",
                        trailing = weekStartLabel,
                        chevron = true,
                        onClick = { showWeekStartDialog = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        label = "Number format",
                        trailing = numberFormatLabel,
                        chevron = true,
                        onClick = { showNumberFormatDialog = true }
                    )
                }

                // ── Finance cycle ──
                SettingsLabel("Finance cycle")
                SettingsGroup {
                    val incomeFormatted = settings.income.formatMoney(
                        settings.currencyCode,
                        settings.regionCode,
                        settings.numberFormat,
                        decimals = 0
                    )
                    SettingsRow(
                        label = "Monthly Income",
                        trailing = incomeFormatted,
                        chevron = true,
                        onClick = { showIncomeDialog = true }
                    )
                    SettingsDivider()
                    SettingsRow(
                        label = "Cycle anchor day",
                        trailing = "Starts on day ${settings.anchorDay}",
                        chevron = true,
                        onClick = { showAnchorDayDialog = true }
                    )
                }

                // Render Selection Dialogs
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
                if (showIncomeDialog) {
                    IncomeEditDialog(
                        current = settings.income,
                        currencyCode = settings.currencyCode,
                        regionCode = settings.regionCode,
                        onSave = onIncomeChange,
                        onDismiss = { showIncomeDialog = false }
                    )
                }
                if (showAnchorDayDialog) {
                    AnchorDayEditDialog(
                        current = settings.anchorDay,
                        onSave = onAnchorDayChange,
                        onDismiss = { showAnchorDayDialog = false }
                    )
                }

                // ── Account ──
                SettingsLabel("Account")
                SettingsGroup {
                    SettingsRow(
                        label    = "Personal details",
                        subLabel = "Name, email, photo",
                        chevron  = true,
                        onClick  = onOpenProfile,
                    )
                    SettingsDivider()
                    SettingsRow(
                        label    = "Manual-only mode",
                        subLabel = "No bank feeds — every entry is yours",
                        trailing = "On",
                    )
                    SettingsDivider()
                    SettingsRow(
                        label    = "Export ledger",
                        subLabel = "Download a CSV of this cycle",
                        chevron  = true,
                        onClick  = {},
                    )
                }

                // Footer
                Spacer(Modifier.height(16.dp))
                Text(
                    text     = "dv/de · v1.0 · made by hand",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}


// ── Shared sub-components ──

@Composable
fun SettingsLabel(text: String) {
    Text(
        text      = text.uppercase(),
        style     = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.14f, androidx.compose.ui.unit.TextUnitType.Em),
        ),
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier  = Modifier.padding(start = 8.dp, top = 14.dp, bottom = 9.dp),
    )
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape         = ShapeSettingsGroup,
        color         = MaterialTheme.colorScheme.surfaceContainer,
        modifier      = Modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsRow(
    label:    String,
    subLabel: String?    = null,
    trailing: String?    = null,
    chevron:  Boolean    = false,
    onClick:  (() -> Unit)? = null,
) {
    val modifier = if (onClick != null)
        Modifier.fillMaxWidth()
    else
        Modifier.fillMaxWidth()

    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color   = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subLabel != null) {
                    Text(
                        text  = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailing != null) {
                Text(
                    text  = trailing,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (chevron) {
                Icon(
                    imageVector        = CwIcons.ChevronRight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier           = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 18.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant,
    )
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
            it.getDisplayName(java.util.Locale.getDefault()).contains(search, ignoreCase = true)
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
                    modifier = Modifier.fillMaxWidth().testTag("currency_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Box(modifier = Modifier.height(300.dp)) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(filtered.size) { index ->
                            val c = filtered[index]
                            val symbol = try { c.symbol } catch (e: Exception) { "" }
                            val label = "${c.currencyCode} - ${c.getDisplayName(java.util.Locale.getDefault())} ($symbol)"
                            Surface(
                                onClick = {
                                    onSelect(c.currencyCode)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (c.currencyCode == current) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
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
                                color = if (r.country == current) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
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
                        color = if (value == current) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
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
                        color = if (value == current) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
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
fun IncomeEditDialog(
    current: Double,
    currencyCode: String,
    regionCode: String,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(if (current > 0.0) current.toInt().toString() else "") }
    val parsed = input.toDoubleOrNull() ?: 0.0
    val isValid = parsed > 0.0
    val currencySymbol = try {
        java.util.Currency.getInstance(currencyCode).getSymbol(java.util.Locale("", regionCode))
    } catch (e: Exception) {
        "£"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Monthly Income") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Specify your baseline income/budget for each finance cycle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("e.g. 3000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("settings_income_field"),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        onSave(parsed)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier.testTag("settings_income_confirm")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AnchorDayEditDialog(
    current: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(current.toString()) }
    val parsed = input.toIntOrNull() ?: 0
    val isValid = parsed in 1..28

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cycle Anchor Day") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose the day of the month when your cycle resets (e.g. 25 for salary day). Must be between 1 and 28.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("e.g. 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("settings_anchor_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                if (input.isNotEmpty() && parsed !in 1..28) {
                    Text(
                        text = "Must be between 1 and 28",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        onSave(parsed)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier.testTag("settings_anchor_confirm")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
