package com.dvide.app.ui.entry

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dvide.app.data.model.Category
import com.dvide.app.ui.components.CategoryChip
import com.dvide.app.ui.components.CwIcons
import com.dvide.app.ui.components.Keypad
import com.dvide.app.ui.theme.ShapeCommitBtn
import com.dvide.app.ui.theme.ShapePill
import com.dvide.app.ui.theme.ShapeSheet
import com.dvide.app.ui.theme.LocalCurrencyFormatter
import com.dvide.app.ui.theme.dvideColors

data class NewTransaction(
    val amount: Double,
    val category: String,
    val kind: String,
    val note: String,
)

private val MODE_SPEND   = "spend"
private val MODE_ASIDE   = "aside"

private val ENTRY_NOTES = mapOf(
    "essentials" to listOf("Groceries", "Rent", "Transport", "Utilities"),
    "lifestyle"  to listOf("Coffee", "Dinner out", "Books", "Streaming"),
    "savings"    to listOf("Emergency fund", "Holiday pot", "Rainy day"),
    "investment" to listOf("Index fund", "Pension top-up", "Shares"),
    "security"   to listOf("Insurance", "Deposit", "Warranty"),
)

@Composable
fun AddTransactionSheet(
    onAdd: (NewTransaction) -> Unit,
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

    var amount   by remember { mutableStateOf("0") }
    var mode     by remember { mutableStateOf(MODE_SPEND) }
    var category by remember { mutableStateOf("essentials") }
    var customCat by remember { mutableStateOf("") }
    var note     by remember { mutableStateOf("") }

    val activeCat = customCat.trim().ifEmpty { category }.lowercase()
    val isCustom  = customCat.trim().isNotEmpty()
    val catColor  = MaterialTheme.dvideColors.categoryColor(activeCat)
    val catSoft   = MaterialTheme.dvideColors.categorySoft(activeCat)

    val spendCats = Category.EXPENSE_KEYS
    val asideCats = Category.ASIDE_KEYS
    val cats      = if (mode == MODE_SPEND) spendCats else asideCats

    val value = amount.toDoubleOrNull() ?: 0.0
    val valid = value > 0

    fun switchMode(m: String) {
        mode     = m
        category = if (m == MODE_SPEND) "essentials" else "savings"
        customCat = ""
    }

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

    fun commit() {
        if (!valid) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val finalNote = note.trim().ifEmpty {
            ENTRY_NOTES[activeCat]?.firstOrNull() ?: activeCat.replaceFirstChar { it.uppercase() }
        }
        onAdd(NewTransaction(
            amount   = value,
            category = activeCat,
            kind     = if (mode == MODE_ASIDE) "aside" else "expense",
            note     = finalNote,
        ))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 22.dp),
    ) {
        // Mode toggle: Spend vs Set aside
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(4.dp),
        ) {
            Surface(
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(16.dp),
                color          = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf(MODE_SPEND to "Spend", MODE_ASIDE to "Set aside").forEach { (m, label) ->
                        Surface(
                            onClick = { switchMode(m) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            color    = if (mode == m) MaterialTheme.colorScheme.surfaceContainerHighest
                                       else Color.Transparent,
                        ) {
                            Text(
                                text     = label,
                                style    = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight(620)),
                                color    = if (mode == m) MaterialTheme.colorScheme.onSurface
                                           else          MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(11.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        // Amount display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = currencySymbol,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (valid) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Text(
                    text  = amount,
                    style = MaterialTheme.typography.displaySmall,
                    color = if (valid) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Note input
        OutlinedTextField(
            value         = note,
            onValueChange = { if (it.length <= 32) note = it },
            placeholder   = {
                Text(
                    "Add a note · e.g. ${ENTRY_NOTES[activeCat]?.firstOrNull() ?: ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor   = catColor,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainer,
            ),
            singleLine = true,
        )

        // Category chips
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            cats.forEach { cat ->
                CategoryChip(
                    categoryKey = cat,
                    selected    = !isCustom && category == cat,
                    onClick     = {
                        category  = cat
                        customCat = ""
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Custom category input
        OutlinedTextField(
            value         = customCat,
            onValueChange = { if (it.length <= 24) customCat = it },
            placeholder   = {
                Text(
                    "or type a category…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor   = catColor,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor   = Color.Transparent,
            ),
            singleLine = true,
        )

        // Keypad
        Keypad(
            onKey    = ::pressKey,
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        )

        // Commit button
        Button(
            onClick  = ::commit,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape    = ShapeCommitBtn,
            enabled  = valid,
            colors   = ButtonDefaults.buttonColors(
                containerColor         = catColor,
                contentColor           = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(imageVector = CwIcons.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text  = "${if (mode == MODE_ASIDE) "Set aside" else "Add"} ${Category.labelOf(activeCat).lowercase()}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight(680)),
            )
        }
    }
}
