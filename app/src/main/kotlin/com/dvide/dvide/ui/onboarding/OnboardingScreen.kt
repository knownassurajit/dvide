package com.dvide.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dvide.app.ui.components.CwIcons
import com.dvide.app.ui.settings.CurrencySelectDialog
import com.dvide.app.ui.settings.RegionSelectDialog
import com.dvide.app.ui.settings.WeekStartSelectDialog
import com.dvide.app.ui.settings.NumberFormatSelectDialog
import com.dvide.app.ui.theme.ShapeCommitBtn
import com.dvide.app.ui.theme.ShapeSettingsGroup

@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        email: String,
        income: Double,
        anchorDay: Int,
        currencyCode: String,
        regionCode: String,
        weekStartDay: Int,
        numberFormat: String
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(1) }

    // Persona
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Finance Engine
    var incomeStr by remember { mutableStateOf("") }
    var anchorDayStr by remember { mutableStateOf("25") }

    // Regional
    var currencyCode by remember {
        mutableStateOf(
            try {
                java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode
            } catch (e: Exception) {
                "GBP"
            }
        )
    }
    var regionCode by remember {
        mutableStateOf(
            run {
                val country = java.util.Locale.getDefault().country
                if (country.isNullOrEmpty()) "GB" else country
            }
        )
    }
    var weekStartDay by remember { mutableStateOf(2) } // Monday
    var numberFormat by remember { mutableStateOf("DEFAULT") }

    // Dialog flags
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showWeekStartDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }

    // Validation helpers
    val isPersonaValid = name.trim().isNotEmpty()

    val parsedIncome = incomeStr.toDoubleOrNull() ?: 0.0
    val parsedAnchorDay = anchorDayStr.toIntOrNull() ?: 0
    val isFinanceValid = parsedIncome > 0.0 && parsedAnchorDay in 1..28

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding/header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Logo brand text "dv/de"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "dv",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "de",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Page indicator / steps
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val active = index + 1 == step
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                        )
                    }
                }
            }

            // Central slide wizard with animations
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(spring(stiffness = 300f)) { it } + fadeIn()) togetherWith
                                (slideOutHorizontally(spring(stiffness = 300f)) { -it / 4 } + fadeOut())
                    } else {
                        (slideInHorizontally(spring(stiffness = 300f)) { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally(spring(stiffness = 300f)) { it / 4 } + fadeOut())
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "onboarding_slide"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (currentStep) {
                        1 -> {
                            // Step 1: Persona Details
                            Text(
                                text = "Let's establish your persona",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Dvide is a private, manual ledger built for absolute control. Everything stays safely on your device.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Name field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Name",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("Enter your name") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_name_field"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                    singleLine = true
                                )
                            }

                            // Email field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Email (optional)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("Enter your email") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_email_field"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                        2 -> {
                            // Step 2: Finance Engine settings
                            Text(
                                text = "Configure your Finance Cycle",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Establish your income budget and when your financial cycles reset (typically payday).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Income field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val symbol = try {
                                    java.util.Currency.getInstance(currencyCode).getSymbol(java.util.Locale("", regionCode))
                                } catch (e: Exception) {
                                    "£"
                                }
                                Text(
                                    text = "Monthly income/budget ($symbol)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = incomeStr,
                                    onValueChange = { incomeStr = it },
                                    placeholder = { Text("e.g. 3000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_income_field"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                    singleLine = true
                                )
                            }

                            // Anchor Day field
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Cycle anchor day (1-28)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = anchorDayStr,
                                    onValueChange = { anchorDayStr = it },
                                    placeholder = { Text("e.g. 25") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("onboard_anchor_field"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                    singleLine = true
                                )
                                if (anchorDayStr.isNotEmpty() && parsedAnchorDay !in 1..28) {
                                    Text(
                                        text = "Must be between 1 and 28",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                        3 -> {
                            // Step 3: Localization Preferences
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
                        if (step < 3) {
                            step += 1
                        } else {
                            onComplete(
                                name.trim(),
                                email.trim(),
                                parsedIncome,
                                parsedAnchorDay,
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
                        2 -> isFinanceValid
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
                        text = if (step == 3) "Build workspace" else "Continue",
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
