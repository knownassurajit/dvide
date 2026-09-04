package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.domain.model.PastCycle
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    pastCycles: List<PastCycle>,
    onClose: () -> Unit,
    onOpenCycle: (PastCycle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formatter = LocalCurrencyFormatter.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Archive",
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
        modifier = modifier
    ) { paddingValues ->
        if (pastCycles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No closed cycles yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "When a pay window ends, it lands here with its closing balance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = DvideDimens.screen, vertical = DvideDimens.item),
                verticalArrangement = Arrangement.spacedBy(DvideDimens.item)
            ) {
                items(pastCycles) { cycle ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().clickable { onOpenCycle(cycle) }
                    ) {
                         Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DvideDimens.list, vertical = DvideDimens.list),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cycle.label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(cycle.range, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text(
                                text = "${if (cycle.balance >= 0) "+" else "−"}${formatter.format(kotlin.math.abs(cycle.balance))}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (cycle.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
