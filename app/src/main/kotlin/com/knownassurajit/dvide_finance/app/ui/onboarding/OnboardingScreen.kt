package com.knownassurajit.dvide_finance.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.settings.CurrencySelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.NumberFormatSelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.RegionSelectDialog
import com.knownassurajit.dvide_finance.app.ui.settings.WeekStartSelectDialog
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
        numberFormat: String
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // State for Step 1: Persona
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val isPersonaValid = name.isNotBlank() && email.isNotBlank() && email.contains("@")

    // State for Step 2: Localization
    var currencyCode by remember { mutableStateOf(try { java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode } catch (e: Exception) { "GBP" }) }
    var regionCode by remember {
        mutableStateOf(try {
            val c = java.util.Locale.getDefault().country
            if (c.isNullOrEmpty()) "GB" else c
        } catch (e: Exception) { "GB" })
    }
    var weekStartDay by remember { mutableIntStateOf(2) } // Monday default
    var numberFormat by remember { mutableStateOf("DEFAULT") }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Step Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    val isActive = idx + 1 <= step
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 12.dp else 8.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated content transitions between steps
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    label = "onboarding_step"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Welcome to Dvide",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Let's personalize your finance workspace.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

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
                                    )
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Your Email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_email"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    )
                                )
                            }
                        }
                        2 -> {
                            // Step 2: Localization Preferences
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Customize regional options",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Review defaults or change your preferred symbol formatting and calendar boundaries.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val currencySymbol = try {
                                    java.util.Currency.getInstance(currencyCode).getSymbol(java.util.Locale("", regionCode))
                                } catch (e: Exception) {
                                    "£"
                                }
                                val regionLabel = try {
                                    java.util.Locale("", regionCode).displayCountry
                                } catch (e: Exception) {
                                    "United Kingdom"
                                }
                                val weekStartLabel = when (weekStartDay) {
                                    java.util.Calendar.SATURDAY -> "Saturday"
                                    java.util.Calendar.SUNDAY -> "Sunday"
                                    else -> "Monday"
                                }
                                val numberFormatLabel = when (numberFormat) {
                                    "DOT_DECIMAL" -> "1,234.56"
                                    "COMMA_DECIMAL" -> "1.234,56"
                                    "SPACE_DECIMAL" -> "1 234,56"
                                    else -> "Default for Region"
                                }

                                Surface(
                                    shape = ShapeSettingsGroup,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        OnboardingRow(
                                            label = "Currency",
                                            value = "$currencyCode · $currencySymbol",
                                            onClick = { showCurrencyDialog = true }
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow(
                                            label = "Region",
                                            value = regionLabel,
                                            onClick = { showRegionDialog = true }
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow(
                                            label = "Week starts on",
                                            value = weekStartLabel,
                                            onClick = { showWeekStartDialog = true }
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                        OnboardingRow(
                                            label = "Number format",
                                            value = numberFormatLabel,
                                            onClick = { showNumberFormatDialog = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lower Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = ShapeCommitBtn,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (step < 2) {
                            step += 1
                        } else {
                            onComplete(
                                name.trim(),
                                email.trim(),
                                currencyCode,
                                regionCode,
                                weekStartDay,
                                numberFormat
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(if (step > 1) 1f else 2f)
                        .height(58.dp)
                        .testTag("onboard_next_button"),
                    shape = ShapeCommitBtn,
                    enabled = when (step) {
                        1 -> isPersonaValid
                        else -> true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (step == 2) "Build workspace" else "Continue",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Render configuration dialogs
    if (showCurrencyDialog) {
        CurrencySelectDialog(
            current = currencyCode,
            onSelect = { currencyCode = it },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    if (showRegionDialog) {
        RegionSelectDialog(
            current = regionCode,
            onSelect = { regionCode = it },
            onDismiss = { showRegionDialog = false }
        )
    }
    if (showWeekStartDialog) {
        WeekStartSelectDialog(
            current = weekStartDay,
            onSelect = { weekStartDay = it },
            onDismiss = { showWeekStartDialog = false }
        )
    }
    if (showNumberFormatDialog) {
        NumberFormatSelectDialog(
            current = numberFormat,
            onSelect = { numberFormat = it },
            onDismiss = { showNumberFormatDialog = false }
        )
    }
}

@Composable
private fun OnboardingRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = CwIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
