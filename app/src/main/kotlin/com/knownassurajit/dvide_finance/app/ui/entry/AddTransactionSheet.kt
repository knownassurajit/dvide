package com.knownassurajit.dvide_finance.app.ui.entry

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.ui.components.CategoryChip
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.DatePickerField
import com.knownassurajit.dvide_finance.app.ui.components.Keypad
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.M3XMotion
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect
import java.time.LocalDate

data class NewTransaction(
    val amount: Double,
    val category: String,
    val kind: String,
    val note: String,
    val date: LocalDate = LocalDate.now(),
    val color: Int? = null,
    val cycleId: Long? = null,
)

private const val MODE_SPEND = "spend"
private const val MODE_ASIDE = "aside"

private val ENTRY_NOTES = mapOf(
    "essentials" to listOf("Groceries", "Rent", "Transport", "Utilities"),
    "lifestyle"  to listOf("Coffee", "Dinner out", "Books", "Streaming"),
    "savings"    to listOf("Emergency fund", "Holiday pot", "Rainy day"),
    "investment" to listOf("Index fund", "Pension top-up", "Shares"),
    "security"   to listOf("Insurance", "Deposit", "Warranty"),
)

val ENTRY_COLOR_SWATCHES = listOf(
    null to "Default",
    0xFF4285F4.toInt() to "Blue",
    0xFF34A853.toInt() to "Green",
    0xFFFBBC05.toInt() to "Amber",
    0xFFEA4335.toInt() to "Coral",
    0xFFA142F4.toInt() to "Purple",
    0xFF00BCD4.toInt() to "Cyan",
    0xFFE91E63.toInt() to "Pink",
    0xFFFF9800.toInt() to "Orange",
)

@Composable
fun AddTransactionSheet(
    onAdd: (NewTransaction) -> Unit,
    modifier: Modifier = Modifier,
    editing: Transaction? = null,
    onUpdate: ((Transaction) -> Unit)? = null,
    cycleId: Long? = null,
    cycleStart: LocalDate? = null,
    cycleEnd: LocalDate? = null,
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

    var amount by remember(editing) {
        mutableStateOf(
            if (editing != null) {
                if (editing.amount % 1.0 == 0.0) editing.amount.toLong().toString()
                else String.format(java.util.Locale.US, "%.2f", editing.amount)
            } else "0"
        )
    }
    var mode by remember(editing) {
        mutableStateOf(if (editing?.kind == "aside") MODE_ASIDE else MODE_SPEND)
    }
    var category by remember(editing) {
        mutableStateOf(editing?.category ?: "essentials")
    }
    var customCat by remember { mutableStateOf("") }
    var note by remember(editing) { mutableStateOf(editing?.note ?: "") }
    var selectedColor by remember(editing) { mutableStateOf(editing?.color) }
    var entryDate by remember(editing) {
        mutableStateOf(editing?.date ?: LocalDate.now())
    }

    val activeCat = customCat.trim().ifEmpty { category }.lowercase()
    val isCustom  = customCat.trim().isNotEmpty()
    val baseCatColor = MaterialTheme.dvideColors.categoryColor(activeCat)
    val catColor = selectedColor?.let { Color(it) } ?: baseCatColor

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
        val kind = if (mode == MODE_ASIDE) "aside" else "expense"
        if (editing != null && onUpdate != null) {
            onUpdate(
                editing.copy(
                    amount = value,
                    category = activeCat,
                    kind = kind,
                    note = finalNote,
                    date = entryDate,
                    color = selectedColor,
                    cycleId = editing.cycleId ?: cycleId,
                )
            )
        } else {
            onAdd(
                NewTransaction(
                    amount   = value,
                    category = activeCat,
                    kind     = kind,
                    note     = finalNote,
                    date     = entryDate,
                    color    = selectedColor,
                    cycleId  = cycleId,
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DvideDimens.screen)
            .padding(bottom = DvideDimens.section),
    ) {
        // Mode toggle: Spend vs Set aside
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DvideDimens.item),
        ) {
            listOf(MODE_SPEND to "Spend", MODE_ASIDE to "Set aside").forEachIndexed { index, (m, label) ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { switchMode(m) },
                    shape = SegmentedButtonDefaults.itemShape(index, 2),
                    label = { Text(label) },
                )
            }
        }

        // Amount display with proper currency alignment
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
                    text  = currencySymbol,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (valid) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text  = amount,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (valid) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        DatePickerField(
            label = "Date",
            date = entryDate,
            onDateChange = { entryDate = it },
            modifier = Modifier.padding(bottom = 8.dp),
        )

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
            modifier              = Modifier.fillMaxWidth().padding(bottom = 14.dp),
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor   = catColor,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor   = Color.Transparent,
            ),
            singleLine = true,
        )

        // Entry Accent Color Swatches
        Text(
            text = "Entry color",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ENTRY_COLOR_SWATCHES.forEach { (colorVal, _) ->
                val isSelected = selectedColor == colorVal
                val swatchColor = if (colorVal == null) baseCatColor else Color(colorVal)

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .clickable {
                            selectedColor = colorVal
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            } else {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = CwIcons.Check,
                            contentDescription = "Selected",
                            tint = if (colorVal == null) MaterialTheme.colorScheme.surface else Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    } else if (colorVal == null) {
                        Text(
                            text = "Auto",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            }
        }

        // Keypad
        Keypad(
            onKey    = ::pressKey,
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        )

        // Commit button
        Button(
            onClick  = ::commit,
            modifier = Modifier
                .fillMaxWidth()
                .height(DvideDimens.commit)
                .m3xPressEffect(),
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
                text  = if (editing != null) "Save changes"
                        else "${if (mode == MODE_ASIDE) "Set aside" else "Add"} ${Category.labelOf(activeCat).lowercase()}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight(680)),
            )
        }
    }
}
