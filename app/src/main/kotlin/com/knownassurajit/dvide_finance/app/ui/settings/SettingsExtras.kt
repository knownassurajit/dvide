package com.knownassurajit.dvide_finance.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.BuildConfig
import com.knownassurajit.dvide_finance.app.data.backup.BackupParse
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.domain.model.SwipeSide
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons

/** Picks what a swipe in one direction does. Delete still asks for confirmation before anything is removed. */
@Composable
fun SwipeActionDialog(
    side: SwipeSide,
    current: SwipeAction,
    onSelect: (SwipeAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        SwipeAction.EDIT to "Open the entry editor",
        SwipeAction.DELETE to "Ask before deleting the entry",
        SwipeAction.NONE to "Swiping this way does nothing",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (side == SwipeSide.LEFT) "Swipe left" else "Swipe right") },
        text = {
            Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (action, description) ->
                    val selected = action == current
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onSelect(action); onDismiss() },
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = action.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = CwIcons.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Restore replaces all data, so the user sees exactly what will be loaded and must confirm. */
@Composable
fun RestoreConfirmDialog(
    backup: BackupParse.Success,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val snapshot = backup.snapshot
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace all data?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val saved = backup.meta.exportedAt?.take(10)?.let { " saved on $it" } ?: ""
                Text(
                    text = "This backup$saved contains ${snapshot.cycles.size} cycle${if (snapshot.cycles.size == 1) "" else "s"} " +
                        "and ${snapshot.transactions.size} entr${if (snapshot.transactions.size == 1) "y" else "ies"}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Restoring removes all current cycles, entries and the backed-up settings. " +
                        "Back up your current data first if you may need it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (backup.warnings.isNotEmpty()) {
                    Text(
                        text = backup.warnings.joinToString(separator = "\n") { "• $it" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Replace", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private object AppInfo {
    const val DEVELOPER = "Surajit Das"
    const val REPO_URL = "https://github.com/knownassurajit/dvide"
}

/** Quiet, secondary footer: tagline, version, developer credit and the source link. */
@Composable
fun DeveloperCredits(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "dv/de · divide each cycle",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Designed and built by ${AppInfo.DEVELOPER}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = {
                // No browser installed → the handler throws; nothing useful to do but stay put.
                try { uriHandler.openUri(AppInfo.REPO_URL) } catch (_: IllegalArgumentException) {}
            },
        ) {
            Text("Source code on GitHub", style = MaterialTheme.typography.labelMedium)
        }
    }
}
