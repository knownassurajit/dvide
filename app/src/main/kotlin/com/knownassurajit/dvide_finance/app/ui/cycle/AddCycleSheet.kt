package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.ui.components.DatePickerField
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddCycleSheet(
    existingCycles: List<ManualCycle>,
    onAdd: (ManualCycle) -> Unit,
    modifier: Modifier = Modifier,
    editing: ManualCycle? = null,
    payday: Int = 25,
) {
    val formatter = LocalCurrencyFormatter.current
    val suggested = remember(existingCycles, payday, editing) {
        if (editing != null) editing.startDate to editing.endDate
        else CycleEngine.suggestedNextWindow(existingCycles, payday, LocalDate.now())
    }

    var step by remember { mutableIntStateOf(1) }
    var startDate by remember { mutableStateOf<LocalDate?>(suggested.first) }
    var endDate by remember { mutableStateOf<LocalDate?>(suggested.second) }
    var incomeInput by remember {
        mutableStateOf(if (editing != null && editing.income > 0) editing.income.toInt().toString() else "")
    }

    val isDateValid = startDate != null && endDate != null && !startDate!!.isAfter(endDate!!)
    val hasOverlap = startDate != null && endDate != null && CycleEngine.cyclesOverlap(
        start = startDate!!,
        end = endDate!!,
        existing = existingCycles,
        ignoreId = editing?.id,
    )
    val dateError = when {
        startDate != null && endDate != null && startDate!!.isAfter(endDate!!) ->
            "Start date must be before end date"
        hasOverlap -> "Cycle dates overlap with an existing cycle"
        else -> null
    }
    val parsedIncome = incomeInput.toDoubleOrNull() ?: 0.0
    val isIncomeValid = parsedIncome > 0
    val isEditing = editing != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DvideDimens.screen)
            .padding(bottom = DvideDimens.section),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AnimatedContent(targetState = step, label = "add_cycle_step") { currentStep ->
            when (currentStep) {
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (isEditing) "Edit period" else "Cycle period",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Pay window for this salary cycle. Defaults follow payday $payday.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DatePickerField(
                            label = "Start",
                            date = startDate,
                            onDateChange = { startDate = it },
                        )
                        DatePickerField(
                            label = "End",
                            date = endDate,
                            onDateChange = { endDate = it },
                            minDate = startDate,
                        )
                        if (dateError != null) {
                            Text(
                                text = dateError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Take-home pay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Income for ${
                                startDate?.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())) ?: "this"
                            } – ${
                                endDate?.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())) ?: "cycle"
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d{0,9}([.]\\d{0,2})?$"))) {
                                    incomeInput = value
                                }
                            },
                            label = { Text("Income") },
                            prefix = {
                                val symbol = try {
                                    java.util.Currency.getInstance(formatter.currencyCode)
                                        .getSymbol(java.util.Locale("", formatter.regionCode))
                                } catch (_: Exception) { "" }
                                Text(symbol)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cycle_income"),
                            singleLine = true,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(DvideDimens.commit),
                    shape = ShapeCommitBtn,
                ) { Text("Back") }
            }
            Button(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        val start = startDate ?: return@Button
                        val end = endDate ?: return@Button
                        onAdd(
                            ManualCycle(
                                id = editing?.id ?: 0,
                                month = start.monthValue,
                                year = start.year,
                                startDate = start,
                                endDate = end,
                                income = parsedIncome,
                            )
                        )
                    }
                },
                modifier = Modifier
                    .weight(if (step == 1) 1f else 2f)
                    .height(DvideDimens.commit)
                    .testTag("cycle_save_button"),
                shape = ShapeCommitBtn,
                enabled = if (step == 1) isDateValid && !hasOverlap else isIncomeValid,
            ) {
                Text(if (step == 1) "Next" else if (isEditing) "Save changes" else "Save cycle")
            }
        }
    }
}

@Composable
fun PaydayIncomeForm(
    payday: Int,
    onPaydayChange: (Int) -> Unit,
    incomeInput: String,
    onIncomeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = LocalCurrencyFormatter.current
    val window = remember(payday) { CycleEngine.windowForPayday(payday, LocalDate.now()) }
    val rangeLabel = remember(window) {
        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        "${window.first.format(fmt)} – ${window.second.format(fmt)}"
    }
    val symbol = remember(formatter) {
        try {
            java.util.Currency.getInstance(formatter.currencyCode)
                .getSymbol(java.util.Locale("", formatter.regionCode))
        } catch (_: Exception) { "" }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Payday",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Day ${payday.coerceIn(1, 31)} of each month · this cycle $rangeLabel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = payday.coerceIn(1, 31).toFloat(),
            onValueChange = { onPaydayChange(it.toInt().coerceIn(1, 31)) },
            valueRange = 1f..31f,
            steps = 29,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_payday_slider"),
        )
        OutlinedTextField(
            value = incomeInput,
            onValueChange = { value ->
                if (value.isEmpty() || value.matches(Regex("^\\d{0,9}([.]\\d{0,2})?$"))) {
                    onIncomeChange(value)
                }
            },
            label = { Text("Take-home pay") },
            prefix = { Text(symbol) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboard_income"),
            singleLine = true,
        )
    }
}
