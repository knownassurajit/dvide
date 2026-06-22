package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import java.util.Calendar

@Composable
fun DashHeader(
    userName: String,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "GOOD MORNING"
        hour < 18 -> "GOOD AFTERNOON"
        else      -> "GOOD EVENING"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 56.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .clickable { onOpenProfile() }
                .testTag("dashboard_profile_click")
        ) {
            Text(
                text          = greeting,
                style         = MaterialTheme.typography.labelSmall,
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = androidx.compose.ui.unit.TextUnit(0.16f, androidx.compose.ui.unit.TextUnitType.Em),
            )
            Text(
                text       = userName,
                style      = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color      = MaterialTheme.colorScheme.onSurface,
            )
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector  = if (darkTheme) CwIcons.Sun else CwIcons.Moon,
                    contentDescription = "Toggle theme",
                    tint         = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("dashboard_settings_button")
            ) {
                Icon(
                    imageVector  = CwIcons.Settings,
                    contentDescription = "Settings",
                    tint         = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Pill segmented control — maps to M3 SegmentedButton or custom
// ─────────────────────────────────────────────────────────────
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    small: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape    = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        color    = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            options.forEachIndexed { index, label ->
                val isSelected = index == selected
                Surface(
                    onClick = { onSelect(index) },
                    shape   = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                    color   = if (isSelected) MaterialTheme.colorScheme.primary
                              else            androidx.compose.ui.graphics.Color.Transparent,
                ) {
                    Text(
                        text       = label,
                        style      = if (small) MaterialTheme.typography.labelSmall
                                     else       MaterialTheme.typography.labelMedium,
                        color      = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                     else            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier   = Modifier.padding(
                            horizontal = if (small) 13.dp else 15.dp,
                            vertical   = if (small) 6.dp  else 7.dp,
                        ),
                    )
                }
            }
        }
    }
}
