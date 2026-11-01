package com.knownassurajit.dvide_finance.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.backup.BackupCounts
import com.knownassurajit.dvide_finance.app.data.backup.BackupParse
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.ExportFormat
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.domain.model.SwipeSide
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.HueSlider
import com.knownassurajit.dvide_finance.app.ui.components.PaydayCalendar
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup
import com.knownassurajit.dvide_finance.app.ui.theme.fontFamilyFor
import com.knownassurajit.dvide_finance.app.ui.theme.hexToHue
import com.knownassurajit.dvide_finance.app.ui.theme.hueToPrimaryColor
import com.knownassurajit.dvide_finance.app.ui.theme.toHex
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    onFontChange: (AppFont) -> Unit = {},
    onShowDecimalsChange: (Boolean) -> Unit = {},
    onDarkThemeChange: (Boolean) -> Unit = { onToggleTheme() },
    onDynamicColorChange: (Boolean) -> Unit = {},
    onPaydayChange: (Int) -> Unit = {},
    onExport: () -> Unit = {},
    onExportFormat: ((ExportFormat) -> Unit)? = null,
    onSwipeLeftChange: (SwipeAction) -> Unit = {},
    onSwipeRightChange: (SwipeAction) -> Unit = {},
    onExportBackup: suspend (Uri) -> Result<BackupCounts> = { Result.failure(UnsupportedOperationException()) },
    onReadBackup: suspend (Uri) -> BackupParse = { BackupParse.Failure("Restore isn't available here.") },
    onRestoreBackup: suspend (BackupParse.Success) -> Result<BackupCounts> = { Result.failure(UnsupportedOperationException()) },
    modifier: Modifier = Modifier,
) {
    var showFontDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var swipeDialogSide by remember { mutableStateOf<SwipeSide?>(null) }
    var pendingRestore by remember { mutableStateOf<BackupParse.Success?>(null) }
    var backupBusy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    val createBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            val result = onExportBackup(uri)
            backupBusy = false
            snackbarHost.showSnackbar(
                result.fold(
                    onSuccess = { "Backed up ${it.cycles} cycle${if (it.cycles == 1) "" else "s"} and ${it.transactions} entr${if (it.transactions == 1) "y" else "ies"}." },
                    onFailure = { "Backup failed: ${it.message ?: "the file could not be written"}" },
                )
            )
        }
    }
    val openBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            backupBusy = true
            val parsed = onReadBackup(uri)
            backupBusy = false
            when (parsed) {
                is BackupParse.Success -> pendingRestore = parsed
                is BackupParse.Failure -> snackbarHost.showSnackbar(parsed.reason)
            }
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.surface,
        // Sides and bottom (cutouts, gesture/nav bar, keyboard) are applied once here; the top bar owns the top inset.
        contentWindowInsets = WindowInsets.safeContent.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        modifier       = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (backupBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

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
                    label = "Chameleon mode",
                    subLabel = "Dynamic adaptive color theming",
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Font family",
                    value = settings.fontOption.label,
                    onClick = { showFontDialog = true },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Column(modifier = Modifier.padding(SettingsSpace.inner)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Brand colour",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (settings.dynamicColor) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        if (!settings.dynamicColor) {
                            Text(
                                text = "Disabled",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(SettingsSpace.rowV))
                    HueSlider(
                        hue = settings.seedHue,
                        onHueChange = { onSeedHueChange(it.toInt()) },
                        enabled = settings.dynamicColor,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentHex = remember(settings.seedHue) { hueToPrimaryColor(settings.seedHue).toHex() }
                    var hexInput by remember(settings.seedHue) { mutableStateOf(currentHex) }
                    val activeColor = remember(settings.seedHue) { hueToPrimaryColor(settings.seedHue) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (settings.dynamicColor) activeColor else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = { input ->
                                val clean = input.trim().uppercase()
                                if (clean.length <= 7) {
                                    hexInput = clean
                                    val parsedHue = hexToHue(clean)
                                    if (parsedHue != null) {
                                        onSeedHueChange(parsedHue)
                                    }
                                }
                            },
                            enabled = settings.dynamicColor,
                            placeholder = { Text("#RRGGBB") },
                            label = { Text("Color hex code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                disabledContainerColor = Color.Transparent,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            ),
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

            SettingsSection(title = "Gestures") {
                SettingsRow(
                    label = "Swipe left",
                    value = settings.swipeLeft.label,
                    onClick = { swipeDialogSide = SwipeSide.LEFT },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Swipe right",
                    value = settings.swipeRight.label,
                    onClick = { swipeDialogSide = SwipeSide.RIGHT },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Text(
                    text = "Applies to entries on the home screen. Deleting always asks first, and a long press still opens the entry menu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(SettingsSpace.inner),
                )
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsSwitchRow(
                    label = "Show decimal values",
                    checked = settings.showDecimals,
                    onCheckedChange = onShowDecimalsChange,
                )
            }

            SettingsSection(title = "Data") {
                SettingsRow(
                    label = "Export ledger",
                    value = "Choose format",
                    onClick = {
                        if (onExportFormat != null) {
                            showExportFormatDialog = true
                        } else {
                            onExport()
                        }
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Back up to file",
                    value = "JSON",
                    onClick = { if (!backupBusy) createBackup.launch("dvide-backup-${LocalDate.now()}.json") },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsRow(
                    label = "Restore from file",
                    value = "Replaces all data",
                    onClick = {
                        if (!backupBusy) openBackup.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                    },
                )
            }

            DeveloperCredits(modifier = Modifier.padding(top = 8.dp))

            // Bottom system insets are already applied by the Scaffold; this is just breathing room.
            Spacer(modifier = Modifier.height(DvideDimens.section))
        }
    }

    swipeDialogSide?.let { side ->
        SwipeActionDialog(
            side = side,
            current = if (side == SwipeSide.LEFT) settings.swipeLeft else settings.swipeRight,
            onSelect = { if (side == SwipeSide.LEFT) onSwipeLeftChange(it) else onSwipeRightChange(it) },
            onDismiss = { swipeDialogSide = null },
        )
    }

    pendingRestore?.let { backup ->
        RestoreConfirmDialog(
            backup = backup,
            onConfirm = {
                pendingRestore = null
                scope.launch {
                    backupBusy = true
                    val result = onRestoreBackup(backup)
                    backupBusy = false
                    snackbarHost.showSnackbar(
                        result.fold(
                            onSuccess = { "Restored ${it.cycles} cycle${if (it.cycles == 1) "" else "s"} and ${it.transactions} entr${if (it.transactions == 1) "y" else "ies"}." },
                            onFailure = { "Restore failed, your data was not changed: ${it.message ?: "unknown error"}" },
                        )
                    )
                }
            },
            onDismiss = { pendingRestore = null },
        )
    }

    if (showExportFormatDialog && onExportFormat != null) {
        ExportFormatDialog(
            onSelect = onExportFormat,
            onDismiss = { showExportFormatDialog = false }
        )
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
    if (showFontDialog) {
        FontSelectDialog(
            current = settings.fontOption,
            onSelect = onFontChange,
            onDismiss = { showFontDialog = false }
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
    subLabel: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsSpace.rowH, vertical = SettingsSpace.switchV),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun ExportFormatDialog(
    onSelect: (ExportFormat) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Export ledger",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Choose your preferred export file format:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                ExportFormat.entries.forEach { format ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelect(format)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = format.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = when (format) {
                                        ExportFormat.CSV -> "Standard comma-separated table (.csv)"
                                        ExportFormat.SPREADSHEET -> "Excel & Google Sheets compatible (.tsv)"
                                        ExportFormat.MARKDOWN -> "Formatted GitHub markdown table (.md)"
                                        ExportFormat.TEXT -> "Human-readable plain text summary (.txt)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                imageVector = CwIcons.Export,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
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

@Composable
fun FontSelectDialog(
    current: AppFont,
    onSelect: (AppFont) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Font Family") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppFont.entries.forEach { font ->
                    val isSelected = font == current
                    Surface(
                        onClick = {
                            onSelect(font)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = font.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = fontFamilyFor(font),
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "The quick brown fox jumps · 12345",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = fontFamilyFor(font),
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = CwIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp),
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

