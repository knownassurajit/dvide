package com.knownassurajit.dvide_finance.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.domain.model.SwipeConfig
import com.knownassurajit.dvide_finance.app.ui.components.TransactionTimeline
import com.knownassurajit.dvide_finance.app.ui.entry.AddTransactionSheet
import com.knownassurajit.dvide_finance.app.ui.entry.NewTransaction
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideTheme
import com.knownassurajit.dvide_finance.app.ui.theme.LocalSwipeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate

/** Real gestures and form input against the actual composables (Robolectric). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class InteractionUiTest {

    @get:Rule val rule = createComposeRule()

    private val today = LocalDate.now()
    private val longNote = "n".repeat(EntryLimits.NOTE_MAX)
    private val groceries = Transaction(1, today, "essentials", "expense", 25.0, "Groceries", null, 1)
    private val topUp = Transaction(2, today, "income", "income", 100.0, "Bonus top-up", null, 1)
    private val wordy = Transaction(3, today, "lifestyle", "expense", 9.0, longNote, null, 1)

    private val metrics = CycleEngine.computeMetrics(
        ManualCycle(1, today.monthValue, today.year, today.minusDays(3), today.plusDays(20), 3000.0),
        listOf(groceries, topUp, wordy),
        today,
    )

    private var edited: Transaction? = null
    private var deleted: Transaction? = null

    private fun showTimeline(config: SwipeConfig = SwipeConfig.DEFAULT) {
        edited = null; deleted = null
        rule.setContent {
            DvideTheme {
                CompositionLocalProvider(LocalSwipeConfig provides config) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        TransactionTimeline(
                            metrics = metrics,
                            groupByWeek = false,
                            highlightId = null,
                            onEditTransaction = { edited = it },
                            onDeleteTransaction = { deleted = it },
                        )
                    }
                }
            }
        }
    }

    // ───────────── swipe ─────────────

    @Test fun `swipe left edits by default and nothing is deleted`() {
        showTimeline()
        rule.onNodeWithText("Groceries").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertEquals(groceries, edited)
        assertNull(deleted)
    }

    @Test fun `swipe right asks for confirmation and only deletes after confirming`() {
        showTimeline()
        rule.onNodeWithText("Groceries").performTouchInput { swipeRight() }
        rule.waitForIdle()
        rule.onNodeWithText("Delete entry").assertIsDisplayed()
        assertNull("a swipe alone must never delete", deleted)
        assertNull(edited)

        rule.onNodeWithText("Delete").performClick()
        rule.waitForIdle()
        assertEquals(groceries, deleted)
    }

    @Test fun `cancelling the delete confirmation keeps the entry`() {
        showTimeline()
        rule.onNodeWithText("Groceries").performTouchInput { swipeRight() }
        rule.waitForIdle()
        rule.onNodeWithText("Cancel").performClick()
        rule.waitForIdle()
        assertNull(deleted)
    }

    @Test fun `swapped mapping is honoured`() {
        showTimeline(SwipeConfig(left = SwipeAction.DELETE, right = SwipeAction.EDIT))
        rule.onNodeWithText("Groceries").performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertEquals(groceries, edited)

        rule.onNodeWithText("Groceries").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        rule.onNodeWithText("Delete entry").assertIsDisplayed()
    }

    @Test fun `a direction set to do nothing ignores the swipe`() {
        showTimeline(SwipeConfig(left = SwipeAction.NONE, right = SwipeAction.NONE))
        rule.onNodeWithText("Groceries").performTouchInput { swipeLeft() }
        rule.onNodeWithText("Groceries").performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertNull(edited); assertNull(deleted)
        rule.onAllNodesWithText("Delete entry").assertCountEquals(0)
    }

    @Test fun `top up entries cannot be edited by swipe`() {
        showTimeline()
        rule.onNodeWithText("Bonus top-up").performTouchInput { swipeLeft() }
        rule.waitForIdle()
        assertNull(edited)
    }

    // ───────────── existing interactions are preserved ─────────────

    @Test fun `long press still opens the entry menu`() {
        showTimeline()
        rule.onNodeWithText("Groceries").performTouchInput { longClick() }
        rule.waitForIdle()
        rule.onNodeWithText("Edit entry").assertIsDisplayed()
        rule.onNodeWithText("Delete entry").assertIsDisplayed()
    }

    @Test fun `tapping a row expands it and shows a long note in full`() {
        showTimeline()
        rule.onAllNodesWithText(longNote, useUnmergedTree = true).assertCountEquals(1) // collapsed: one ellipsized line
        rule.onNodeWithText(longNote).performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText(longNote, useUnmergedTree = true).assertCountEquals(2) // plus the full text
        rule.onNodeWithText("Edit").assertExists()
    }

    @Test fun `screen reader users get the swipe actions as custom actions`() {
        showTimeline()
        val node = rule.onNodeWithText("Groceries").fetchSemanticsNode()
        val actions = node.config[SemanticsActions.CustomActions]
        assertEquals(setOf("Edit entry", "Delete entry"), actions.map { it.label }.toSet())
        rule.runOnIdle { actions.first { it.label == "Delete entry" }.action() }
        rule.waitForIdle()
        rule.onNodeWithText("Delete entry").assertIsDisplayed()
    }

    @Test fun `top up entries expose only the delete custom action`() {
        showTimeline()
        val actions = rule.onNodeWithText("Bonus top-up").fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Delete entry"), actions.map { it.label })
    }

    // ───────────── entry editor ─────────────

    private var added: NewTransaction? = null

    private fun showEntrySheet() {
        added = null
        rule.setContent {
            DvideTheme {
                // The sheet scrolls itself; in the app it sits in a height-bounded bottom sheet.
                Box(Modifier.fillMaxSize()) {
                    AddTransactionSheet(onAdd = { added = it })
                }
            }
        }
    }

    @Test fun `note and category accept far more than the old 32 and 24 characters but stop at the limits`() {
        showEntrySheet()
        rule.onNodeWithTag("entry_note_field").performScrollTo().performTextInput("x".repeat(250))
        rule.onNodeWithTag("entry_category_field").performScrollTo().performTextInput("c".repeat(60))
        rule.onNodeWithText("${EntryLimits.NOTE_MAX}/${EntryLimits.NOTE_MAX}").assertExists()
        rule.onNodeWithText("${EntryLimits.CATEGORY_MAX}/${EntryLimits.CATEGORY_MAX}").assertExists()

        rule.onNodeWithText("5").performScrollTo().performClick()
        // The commit button is labelled with the (long) custom category name.
        rule.onNodeWithText("Add " + "c".repeat(EntryLimits.CATEGORY_MAX)).performScrollTo().performClick()
        rule.waitForIdle()
        val tx = added
        assertNotNull(tx)
        assertEquals(EntryLimits.NOTE_MAX, tx!!.note.length)
        assertEquals(EntryLimits.CATEGORY_MAX, tx.category.length)
    }

    @Test fun `a custom color chosen with a hex code is what gets saved`() {
        showEntrySheet()
        rule.onNodeWithText("5").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Choose a custom color").performScrollTo().performClick()
        rule.onNodeWithTag("color_hex_field").performTextReplacement("#112233")
        rule.onNodeWithTag("color_confirm").assertIsEnabled().performClick()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Custom color").assertExists()

        rule.onNodeWithText("Add essentials").performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(0xFF112233.toInt(), added?.color)
    }

    @Test fun `an invalid hex code cannot be confirmed`() {
        showEntrySheet()
        rule.onNodeWithContentDescription("Choose a custom color").performScrollTo().performClick()
        rule.onNodeWithTag("color_hex_field").performTextReplacement("#12")
        rule.onNodeWithTag("color_confirm").assertIsNotEnabled()
        rule.onNodeWithTag("color_hex_field").performTextReplacement("#123456")
        rule.onNodeWithTag("color_confirm").assertIsEnabled()
    }

    @Test fun `leaving the color on automatic saves no override`() {
        showEntrySheet()
        rule.onNodeWithText("5").performScrollTo().performClick()
        rule.onNodeWithText("Add essentials").performScrollTo().performClick()
        rule.waitForIdle()
        assertTrue(added != null)
        assertNull(added?.color)
    }

    // ───────────── profile & settings ─────────────

    @Test fun `profile explains that details are local and rejects a malformed email`() {
        rule.setContent {
            DvideTheme {
                ProfileScreen(settings = AppSettings(userName = "Sam", userEmail = "sam@dvide.app"), onSave = { _, _ -> }, onClose = {})
            }
        }
        rule.onNodeWithText("Local profile").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("profile_save_button").performScrollTo().assertIsEnabled()

        rule.onNodeWithTag("profile_email_field").performTextReplacement("not-an-email")
        rule.onNodeWithTag("profile_save_button").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithText("Enter a valid email address", substring = true).assertExists()

        rule.onNodeWithTag("profile_email_field").performTextReplacement("kim@example.com")
        rule.onNodeWithTag("profile_save_button").performScrollTo().assertIsEnabled()
    }

    private fun showSettings(settings: AppSettings = AppSettings(), onSwipeLeft: (SwipeAction) -> Unit = {}) {
        rule.setContent {
            DvideTheme {
                SettingsScreen(
                    settings = settings,
                    onClose = {}, onToggleTheme = {}, onVariantChange = {}, onSeedHueChange = {}, onOpenProfile = {},
                    onCurrencyChange = {}, onRegionChange = {}, onWeekStartChange = {}, onNumberFormatChange = {},
                    onSwipeLeftChange = onSwipeLeft,
                )
            }
        }
    }

    @Test fun `settings footer credits the developer and links to the source`() {
        showSettings()
        rule.onNodeWithText("Designed and built by Surajit Das").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Source code on GitHub").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Version", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun `settings lists the swipe gestures with their defaults and lets the user change one`() {
        var chosen: SwipeAction? = null
        showSettings(onSwipeLeft = { chosen = it })
        rule.onNodeWithText("Swipe left").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Swipe right").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Edit").performScrollTo().assertIsDisplayed()   // default left value
        rule.onNodeWithText("Delete").performScrollTo().assertIsDisplayed() // default right value

        rule.onNodeWithText("Swipe left").performScrollTo().performClick()
        rule.onNodeWithText("Do nothing").performClick()
        rule.waitForIdle()
        assertEquals(SwipeAction.NONE, chosen)
    }

    @Test fun `settings offers backup and restore`() {
        showSettings()
        rule.onNodeWithText("Back up to file").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Restore from file").performScrollTo().assertIsDisplayed()
    }
}
