package com.knownassurajit.dvide_finance.app

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideTheme
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

        composeTestRule.onNodeWithTag("profile_name_field").assertTextContains("Sam")
        composeTestRule.onNodeWithTag("profile_email_field").assertTextContains("sam@dvide.app")

        composeTestRule.onNodeWithTag("profile_name_field").performTextReplacement("Alice")
        composeTestRule.onNodeWithTag("profile_email_field").performTextReplacement("alice@dvide.app")
        composeTestRule.onNodeWithTag("profile_save_button").performClick()

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
                )
            }
        }

        composeTestRule.onNodeWithText("Currency").performClick()
        composeTestRule.onNodeWithText("USD - US Dollar", substring = true).assertExists()
        composeTestRule.onNodeWithText("USD - US Dollar", substring = true).performClick()
        assertEquals("USD", selectedCurrency)
    }

    @Test
    fun testOnboardingWizardFlow() {
        var completed = false
        var savedName = ""
        var savedEmail = ""
        var savedIncome = 0.0

        composeTestRule.setContent {
            DvideTheme {
                OnboardingScreen(
                    onComplete = { name, email, _, _, _, _, _, income ->
                        completed = true
                        savedName = name
                        savedEmail = email
                        savedIncome = income
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("onboard_name").performTextInput("Bob")
        composeTestRule.onNodeWithTag("onboard_email").performTextInput("bob@dvide.app")
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        composeTestRule.onNodeWithTag("onboard_income").performTextInput("2500")
        composeTestRule.onNodeWithTag("onboard_next_button").assertIsEnabled()
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        assertTrue(completed)
        assertEquals("Bob", savedName)
        assertEquals("bob@dvide.app", savedEmail)
        assertEquals(2500.0, savedIncome, 0.001)
    }
}
