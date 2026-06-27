package com.knownassurajit.dvide_finance.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.settings.CurrencySelectDialog
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class E2ELocalUiTest {

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

        // Input new values via performTextReplacement
        composeTestRule.onNodeWithTag("profile_name_field").performTextReplacement("Alice")
        composeTestRule.onNodeWithTag("profile_email_field").performTextReplacement("alice@dvide.app")

        // Wait for Compose to apply changes and recompose
        composeTestRule.waitForIdle()

        // Verify that the text fields actually contain the inputted values
        composeTestRule.onNodeWithTag("profile_name_field").assertTextContains("Alice")
        composeTestRule.onNodeWithTag("profile_email_field").assertTextContains("alice@dvide.app")

        // Assert that the save button is enabled
        composeTestRule.onNodeWithTag("profile_save_button").assertIsEnabled()

        // Click save after scrolling to it
        composeTestRule.onNodeWithTag("profile_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify callbacks were triggered with new values
        assertEquals("Alice", savedName)
        assertEquals("alice@dvide.app", savedEmail)
    }

    @Test
    fun testCurrencySelectionDialogIsolation() {
        var selectedCurrency = ""

        composeTestRule.setContent {
            DvideTheme {
                CurrencySelectDialog(
                    current = "GBP",
                    onSelect = { selectedCurrency = it },
                    onDismiss = {}
                )
            }
        }

        // Filter by typing "USD" in the search box
        composeTestRule.onNodeWithText("Search currency...").performTextInput("USD")
        composeTestRule.waitForIdle()

        // Select USD in the filtered dialog list
        composeTestRule.onNodeWithText("USD - US Dollar", substring = true).assertExists()
        composeTestRule.onNodeWithText("USD - US Dollar", substring = true).performClick()
        composeTestRule.waitForIdle()

        // Verify currency selection callback was fired
        assertEquals("USD", selectedCurrency)
    }

    @Test
    fun testOnboardingWizardFlow() {
        var completed = false
        var savedName = ""
        var savedEmail = ""

        composeTestRule.setContent {
            DvideTheme {
                OnboardingScreen(
                    onComplete = { name, email, _, _, _, _ ->
                        completed = true
                        savedName = name
                        savedEmail = email
                    }
                )
            }
        }

        // --- STEP 1: Persona ---
        // Input Name and Email
        composeTestRule.onNodeWithTag("onboard_name").performTextInput("Bob")
        composeTestRule.onNodeWithTag("onboard_email").performTextInput("bob@dvide.app")
        // Click Continue
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        // --- STEP 2: Localization Preferences ---
        // Click Build workspace
        composeTestRule.onNodeWithTag("onboard_next_button").performClick()

        // Verify final callback matches user inputs
        assertTrue(completed)
        assertEquals("Bob", savedName)
        assertEquals("bob@dvide.app", savedEmail)
    }
}
