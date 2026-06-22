package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeKeypadKey

@Composable
fun Keypad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        ".", "0", "del",
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(3).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    KeypadKey(
                        label    = key,
                        isAction = key == "del",
                        modifier = Modifier.weight(1f).height(56.dp),
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKey(key)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    isAction: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isAction) {
        FilledTonalIconButton(
            onClick  = onClick,
            modifier = modifier,
            shape    = ShapeKeypadKey,
            colors   = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.Transparent,
                contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(imageVector = CwIcons.Backspace, contentDescription = "Delete")
        }
    } else {
        ElevatedButton(
            onClick        = onClick,
            modifier       = modifier,
            shape          = ShapeKeypadKey,
            colors         = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor   = MaterialTheme.colorScheme.onSurface,
            ),
            elevation      = ButtonDefaults.elevatedButtonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text       = label,
                    fontSize   = 25.sp,
                    fontWeight = FontWeight(560),
                )
            }
        }
    }
}
