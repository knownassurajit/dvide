package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.Keypad
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect

@Composable
fun AddFundsSheet(
    onAddFunds: (amount: Double, note: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val formatter = LocalCurrencyFormatter.current
    val currencySymbol = remember(formatter) {
        try {
            java.util.Currency.getInstance(formatter.currencyCode).getSymbol(java.util.Locale("", formatter.regionCode))
        } catch (e: Exception) {
            "£"
        }
    }

    var amount by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }

    val value = amount.toDoubleOrNull() ?: 0.0
    val valid = value > 0

    fun pressKey(k: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        amount = when (k) {
            "del" -> if (amount.length <= 1) "0" else amount.dropLast(1)
            "."   -> if ("." in amount) amount else "$amount."
            else  -> {
                val hasDot   = "." in amount
                val decimals = if (hasDot) amount.substringAfter('.').length else 0
                when {
                    hasDot && decimals >= 2 -> amount
                    amount.replace(".", "").length >= 7 -> amount
                    amount == "0" && k != "." -> k
                    else -> "$amount$k"
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DvideDimens.screen)
            .padding(bottom = DvideDimens.section),
    ) {
        Text(
            text = "Add extra income or top up your spendable balance for this cycle.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Amount Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 32) note = it },
            placeholder = { Text("Source e.g. Bonus, Side gig, Refund") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            singleLine = true,
        )

        // Keypad
        Keypad(
            onKey = ::pressKey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        )

        // Commit Button
        Button(
            onClick = {
                if (valid) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddFunds(value, note.trim().ifEmpty { "Top-up balance" })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(DvideDimens.commit)
                .m3xPressEffect(),
            shape = ShapeCommitBtn,
            enabled = valid,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(imageVector = CwIcons.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add to balance",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight(680)),
            )
        }
    }
}
