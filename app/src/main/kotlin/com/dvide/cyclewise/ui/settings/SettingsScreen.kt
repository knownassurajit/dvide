package com.dvide.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dvide.app.data.repository.AppSettings
import com.dvide.app.domain.model.DashboardVariant
import com.dvide.app.ui.components.*
import com.dvide.app.ui.theme.ShapeSettingsGroup

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onClose: () -> Unit,
    onToggleTheme: () -> Unit,
    onVariantChange: (DashboardVariant) -> Unit,
    onSeedHueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
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

                // ── Account identity ──
                Row(
                    modifier          = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    // Avatar circle with initial
                    Box(
                        modifier        = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = settings.userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Column {
                        Text(
                            text  = settings.userName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text  = "${settings.userName.lowercase()}@cyclewise.app",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

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
                                        com.dvide.app.data.model.Category.oklchToColor(
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
                    SettingsRow(label = "Currency",       trailing = "GBP · £",     chevron = true)
                    SettingsDivider()
                    SettingsRow(label = "Region",         trailing = "United Kingdom", chevron = true)
                    SettingsDivider()
                    SettingsRow(label = "Week starts on", trailing = "Monday",       chevron = true)
                    SettingsDivider()
                    SettingsRow(label = "Number format",  trailing = "1,234.56",     chevron = true)
                }

                // ── Account ──
                SettingsLabel("Account")
                SettingsGroup {
                    SettingsRow(
                        label    = "Personal details",
                        subLabel = "Name, email, photo",
                        chevron  = true,
                        onClick  = {},
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
                    text     = "Cyclewise · v1.0 · made by hand",
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
