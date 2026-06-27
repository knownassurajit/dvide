package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import com.knownassurajit.dvide_finance.app.ui.theme.LocalCurrencyFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCycleSheet(
    existingCycles: List<ManualCycle>,
    onAdd: (ManualCycle) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1 State: Dates
    var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // Step 2 State: Income
    var incomeInput by remember { mutableStateOf("") }

    // Validation
    val isDateValid = startDate != null && endDate != null && !startDate!!.isAfter(endDate!!)
    val hasOverlap = startDate != null && endDate != null && existingCycles.any { cycle ->
        startDate!!.isBefore(cycle.endDate.plusDays(1)) && endDate!!.isAfter(cycle.startDate.minusDays(1))
    }

    val dateError = when {
        startDate != null && endDate != null && startDate!!.isAfter(endDate!!) -> "Start date must be before end date"
        hasOverlap -> "Cycle dates overlap with an existing cycle"
        else -> null
    }

    val parsedIncome = incomeInput.toDoubleOrNull() ?: 0.0
    val isIncomeValid = parsedIncome > 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AnimatedContent(
            targetState = step,
            label = "add_cycle_step"
        ) { currentStep ->
            when (currentStep) {
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Cycle Period",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Month Dropdown (Simplified for now - can use a real dropdown component later)
                            OutlinedTextField(
                                value = selectedMonth.toString(),
                                onValueChange = { selectedMonth = it.toIntOrNull() ?: 1 },
                                label = { Text("Month (1-12)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = selectedYear.toString(),
                                onValueChange = { selectedYear = it.toIntOrNull() ?: LocalDate.now().year },
                                label = { Text("Year") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Basic Date Pickers (In a real app, use DatePickerDialog)
                         Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startDate?.toString() ?: "",
                                onValueChange = {
                                    try { startDate = LocalDate.parse(it) } catch(e:Exception){}
                                },
                                label = { Text("Start Date (yyyy-mm-dd)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endDate?.toString() ?: "",
                                onValueChange = {
                                     try { endDate = LocalDate.parse(it) } catch(e:Exception){}
                                },
                                label = { Text("End Date (yyyy-mm-dd)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (dateError != null) {
                             Text(
                                text = dateError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                2 -> {
                     Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Cycle Income",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { incomeInput = it },
                            label = { Text("Income") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                     }
                }
            }
        }

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step = 1 },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = ShapeCommitBtn
                ) {
                    Text("Back")
                }
            }

            Button(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        onAdd(
                            ManualCycle(
                                month = selectedMonth,
                                year = selectedYear,
                                startDate = startDate!!,
                                endDate = endDate!!,
                                income = parsedIncome
                            )
                        )
                    }
                },
                modifier = Modifier.weight(if (step == 1) 1f else 2f).height(56.dp),
                shape = ShapeCommitBtn,
                enabled = if (step == 1) isDateValid && !hasOverlap else isIncomeValid
            ) {
                Text(if (step == 1) "Next" else "Save Cycle")
            }
        }
    }
}
