package com.knownassurajit.dvide_finance.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.cycle.PaydayIncomeForm
import com.knownassurajit.dvide_finance.app.ui.settings.CurrencySelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.NumberFormatSelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.RegionSelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.WeekStartSelectDialog
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSettingsGroup

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        email: String,
        currencyCode: String,
        regionCode: String,
        weekStartDay: Int,
        numberFormat: String,
        payday: Int,
        income: Double,
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isPersonaValid = name.isNotBlank() && email.isNotBlank() && email.contains("@")

    var currencyCode by remember {
        mutableStateOf(
            try {
                java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode
            } catch (_: Exception) { "GBP" }
        )
    }
    var regionCode by remember {
        mutableStateOf(
            try {
                val c = java.util.Locale.getDefault().country
                if (c.isNullOrEmpty()) "GB" else c
            } catch (_: Exception) { "GB" }
        )
    }
    var weekStartDay by remember { mutableIntStateOf(2) }
    var numberFormat by remember { mutableStateOf("DEFAULT") }

    var payday by remember { mutableIntStateOf(25) }
    var incomeInput by remember { mutableStateOf("") }
    val parsedIncome = incomeInput.toDoubleOrNull() ?: 0.0
    val isCycleValid = parsedIncome > 0

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeContent)
                .padding(DvideDimens.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { idx ->
                    val isActive = idx + 1 <= step
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 12.dp else 8.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(DvideDimens.section))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "onboarding_step",
                ) { currentStep ->
                    when (currentStep) {
                        1 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = "Welcome to dv/de",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "Divide each pay cycle into set-aside, spendable, and a daily allowance.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("What should we call you?") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_name"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Your email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_email"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                )
                            }
                        }
                        2 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = "Regional options",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "Currency, calendar, and number formatting for this device.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                val currencySymbol = try {
                                    java.util.Currency.getInstance(currencyCode)
                                        .getSymbol(java.util.Locale("", regionCode))
                                } catch (_: Exception) { "£" }
                                val regionLabel = try {
                                    java.util.Locale("", regionCode).displayCountry
                                } catch (_: Exception) { "United Kingdom" }
                                val weekStartLabel = when (weekStartDay) {
                                    java.util.Calendar.SATURDAY -> "Saturday"
                                    java.util.Calendar.SUNDAY -> "Sunday"
                                    else -> "Monday"
                                }
                                val numberFormatLabel = when (numberFormat) {
                                    "DOT_DECIMAL" -> "1,234.56"
                                    "COMMA_DECIMAL" -> "1.234,56"
                                    "SPACE_DECIMAL" -> "1 234,56"
                                    else -> "Default for region"
                                }
                                Surface(
                                    shape = ShapeSettingsGroup,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column {
                                        OnboardingRow("Currency", "$currencyCode · $currencySymbol") {
                                            showCurrencyDialog = true
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow("Region", regionLabel) { showRegionDialog = true }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow("Week starts on", weekStartLabel) { showWeekStartDialog = true }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow("Number format", numberFormatLabel) { showNumberFormatDialog = true }
                                    }
                                }
                            }
                        }
                        else -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = "Your pay cycle",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "DVIDE anchors a repeating window to payday, then divides income into set-aside and spendable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                PaydayIncomeForm(
                                    payday = payday,
                                    onPaydayChange = { payday = it },
                                    incomeInput = incomeInput,
                                    onIncomeChange = { incomeInput = it },
                                    weekStartDay = weekStartDay,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(DvideDimens.commit),
                        shape = ShapeCommitBtn,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        when (step) {
                            1, 2 -> step += 1
                            else -> onComplete(
                                name.trim(),
                                email.trim(),
                                currencyCode,
                                regionCode,
                                weekStartDay,
                                numberFormat,
                                payday,
                                parsedIncome,
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(if (step > 1) 1f else 2f)
                        .height(DvideDimens.commit)
                        .testTag("onboard_next_button"),
                    shape = ShapeCommitBtn,
                    enabled = when (step) {
                        1 -> isPersonaValid
                        2 -> true
                        else -> isCycleValid
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = when (step) {
                            1 -> "Continue"
                            2 -> "Continue"
                            else -> "Build workspace"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectDialog(
            current = currencyCode,
            onSelect = { currencyCode = it },
            onDismiss = { showCurrencyDialog = false },
        )
    }
    if (showRegionDialog) {
        RegionSelectDialog(
            current = regionCode,
            onSelect = { regionCode = it },
            onDismiss = { showRegionDialog = false },
        )
    }
    if (showWeekStartDialog) {
        WeekStartSelectDialog(
            current = weekStartDay,
            onSelect = { weekStartDay = it },
            onDismiss = { showWeekStartDialog = false },
        )
    }
    if (showNumberFormatDialog) {
        NumberFormatSelectDialog(
            current = numberFormat,
            onSelect = { numberFormat = it },
            onDismiss = { showNumberFormatDialog = false },
        )
    }
}

@Composable
private fun OnboardingRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = DvideDimens.list, vertical = DvideDimens.list),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = CwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
