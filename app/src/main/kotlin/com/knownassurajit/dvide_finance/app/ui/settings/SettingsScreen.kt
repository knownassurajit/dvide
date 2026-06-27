package com.knownassurajit.dvide_finance.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.HueSlider
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup
import com.knownassurajit.dvide_finance.app.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
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
        modifier       = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Personal & Profile
            SettingsSection(title = "Profile") {
                SettingsRow(
                    label = "Account Details",
                    value = settings.userName.ifEmpty { "Not set" },
                    onClick = onOpenProfile,
                )
            }

            // Appearance & Dashboard
            SettingsSection(title = "Appearance") {
                SettingsRow(
                    label = "Dark Mode",
                    value = if (settings.darkTheme) "On" else "Off",
                    onClick = onToggleTheme,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Dashboard Layout",
                    value = settings.dashboardVariant.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = {
                        val next = DashboardVariant.entries[(settings.dashboardVariant.ordinal + 1) % DashboardVariant.entries.size]
                        onVariantChange(next)
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Brand Color Seed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HueSlider(
                        hue = settings.seedHue,
                        onHueChange = { onSeedHueChange(it.toInt()) },
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

            Spacer(modifier = Modifier.height(48.dp))
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
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
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
            .padding(horizontal = 18.dp, vertical = 18.dp),
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
