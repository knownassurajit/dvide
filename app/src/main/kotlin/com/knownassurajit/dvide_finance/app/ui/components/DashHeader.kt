package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashHeader(
    userName: String,
    viewIsWeekly: Boolean,
    onViewChange: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddCycle: () -> Unit,
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else      -> "Good evening"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(top = DvideDimens.hairline, bottom = DvideDimens.item),
        verticalArrangement = Arrangement.spacedBy(DvideDimens.item),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = DvideDimens.screen, end = DvideDimens.barInset),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenProfile() }
                    .padding(end = DvideDimens.tight)
                    .testTag("dashboard_profile_click"),
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = userName.ifBlank { "You" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onOpenArchive) {
                Icon(
                    imageVector = CwIcons.History,
                    contentDescription = "Archive",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onAddCycle) {
                Icon(
                    imageVector = CwIcons.Plus,
                    contentDescription = "Add Cycle",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("dashboard_settings_button"),
            ) {
                Icon(
                    imageVector = CwIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DvideDimens.screen),
        ) {
            val options = listOf("Day", "Week")
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = if (index == 0) !viewIsWeekly else viewIsWeekly,
                    onClick = { onViewChange(index == 1) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    label = { Text(label) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    small: Boolean = false,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selected,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                label = {
                    Text(
                        text = label,
                        style = if (small) MaterialTheme.typography.labelMedium
                                else MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}
