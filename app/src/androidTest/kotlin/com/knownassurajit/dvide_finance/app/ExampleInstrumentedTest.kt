package com.knownassurajit.dvide_finance.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideTheme
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class E2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileScreenFlow() {
        var savedName = ""
        var savedEmail = ""
        val settings = AppSettings(userName = "Sam", userEmail = "sam@dvide.app")

        composeTestRule.setContent {
            DvideTheme {
                ProfileScreen(
                    settings = settings,
                    onSave = { name, email ->
                        savedName = name
                        savedEmail = email
                    },
                    onClose = {}
                )
            }
        }

        // Verify initial values
        composeTestRule.onNodeWithTag("profile_name_field").assertTextContains("Sam")
        composeTestRule.onNodeWithTag("profile_email_field").assertTextContains("sam@dvide.app")

        // Input new values
        composeTestRule.onNodeWithTag("profile_name_field").performTextReplacement("Alice")
        composeTestRule.onNodeWithTag("profile_email_field").performTextReplacement("alice@dvide.app")

        // Click save
        composeTestRule.onNodeWithTag("profile_save_button").performClick()

        // Verify callbacks were triggered with new values
        assertEquals("Alice", savedName)
        assertEquals("alice@dvide.app", savedEmail)
    }

    @Test
    fun testSettingsScreenCurrencySelection() {
        var selectedCurrency = ""
        val settings = AppSettings(currencyCode = "GBP")

        composeTestRule.setContent {
            DvideTheme {
                SettingsScreen(
                    settings = settings,
                    onClose = {},
                    onToggleTheme = {},
                    onVariantChange = {},
                    onSeedHueChange = {},
                    onOpenProfile = {},
                    onCurrencyChange = { selectedCurrency = it },
                    onRegionChange = {},
                    onWeekStartChange = {},
                    onNumberFormatChange = {},
                    onIncomeChange = {},
                    onAnchorDayChange = {}
                )
            }
        }

        // Open Currency dialog
        composeTestRule.onNodeWithText("Currency").performClick()

        // Select USD in the dialog
        composeTestRule.onNodeWithText("USD - US Dollar ($)", substring = true).assertExists()
        composeTestRule.onNodeWithText("USD - US Dollar ($)", substring = true).performClick()

        // Verify currency selection callback was fired
        assertEquals("USD", selectedCurrency)
    }

    @Test
    fun testOnboardingWizardFlow() {
        var completed = false
        var savedName = ""
        var savedEmail = ""
        var savedIncome = 0.0
        var savedAnchor = 0

        composeTestRule.setContent {
            DvideTheme {
                OnboardingScreen(
                    onComplete = { name, email, income, anchor, _, _, _, _ ->
                        completed = true
                        savedName = name
                        savedEmail = email
                        savedIncome = income
                        savedAnchor = anchor
                    }
                )
            }
        }

        // --- STEP 1: Persona ---
        // Input Name and Email
        composeTestRule.onNodeWithTag("onboard_name_field").performTextInput("Bob")
        composeTestRule.onNodeWithTag("onboard_email_field").performTextInput("bob@dvide.app")
        // Click Continue
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        // --- STEP 2: Finance Cycle ---
        // Input Income and Anchor Day
        composeTestRule.onNodeWithTag("onboard_income_field").performTextInput("2500")
        composeTestRule.onNodeWithTag("onboard_anchor_field").performTextReplacement("15")
        // Click Continue
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        // --- STEP 3: Localization Preferences ---
        // Click Build workspace
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        // Verify final callback matches user inputs
        assertTrue(completed)
        assertEquals("Bob", savedName)
        assertEquals("bob@dvide.app", savedEmail)
        assertEquals(2500.0, savedIncome, 0.001)
        assertEquals(15, savedAnchor)
    }
}
