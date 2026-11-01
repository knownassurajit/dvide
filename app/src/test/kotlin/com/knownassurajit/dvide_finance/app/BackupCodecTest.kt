package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.data.backup.BackupCodec
import com.knownassurajit.dvide_finance.app.data.backup.BackupParse
import com.knownassurajit.dvide_finance.app.data.backup.BackupSettings
import com.knownassurajit.dvide_finance.app.data.backup.BackupSnapshot
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import java.time.LocalDate

// Runs under Robolectric so the tests exercise Android's own org.json (what ships), not a JVM look-alike.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BackupCodecTest {

    private val cycle1 = ManualCycle(1, 8, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 30), 3200.5)
    private val cycle2 = ManualCycle(2, 9, 2026, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 29), 3300.0)

    private val txs = listOf(
        Transaction(1, LocalDate.of(2026, 8, 2), "savings", "aside", 400.0, "Rainy day", null, 1),
        Transaction(2, LocalDate.of(2026, 8, 3), "coffee", "expense", 3.75, "Flat white ☕ \"oat\" / extra", 0xFFEA4335.toInt(), 1),
        Transaction(3, LocalDate.of(2026, 8, 4), "income", "income", 100.0, "Top-up balance", null, 1),
        // cycle 9 no longer exists: the app keeps entries when a cycle is deleted
        Transaction(4, LocalDate.of(2026, 7, 4), "essentials", "expense", 12.0, "", null, 9),
        Transaction(5, LocalDate.of(2026, 8, 5), "lifestyle", "expense", 1.0, "n".repeat(EntryLimits.NOTE_MAX), null, null),
    )

    private val settings = BackupSettings(
        seedHue = 212, darkTheme = false, dynamicColor = true, dashboardVariant = DashboardVariant.GAUGE,
        fontOption = AppFont.INTER, currencyCode = "CAD", regionCode = "CA", weekStartDay = 1,
        numberFormat = "DOT_DECIMAL", showDecimals = false, payday = 31,
        swipeLeft = SwipeAction.DELETE, swipeRight = SwipeAction.NONE, userName = "Sam", userEmail = "sam@example.com",
    )

    private fun snapshot() = BackupSnapshot(listOf(cycle2, cycle1), txs.reversed(), settings)

    private fun encode(s: BackupSnapshot = snapshot()) =
        BackupCodec.encode(s, Instant.parse("2026-11-01T10:00:00Z"), "0.0.0.2")

    private fun success(text: String): BackupParse.Success =
        BackupCodec.decode(text) as? BackupParse.Success ?: error("Expected the backup to decode")

    private fun failureReason(text: String): String =
        (BackupCodec.decode(text) as? BackupParse.Failure)?.reason ?: error("Expected the backup to be rejected")

    /** Starts from a valid encoded file and edits it through the JSON API (independent of text formatting). */
    private fun edit(block: JSONObject.() -> Unit): String = JSONObject(encode()).apply(block).toString()

    // encode() sorts by id, so index 0 is id 1.
    private fun JSONObject.tx(i: Int): JSONObject = getJSONArray("transactions").getJSONObject(i)
    private fun JSONObject.cyc(i: Int): JSONObject = getJSONArray("cycles").getJSONObject(i)

    // ───────────── round trip ─────────────

    @Test
    fun `round trip preserves cycles transactions and settings`() {
        val ok = success(encode())
        assertEquals(listOf(cycle1, cycle2), ok.snapshot.cycles)
        assertEquals(txs, ok.snapshot.transactions)
        assertEquals(settings, ok.snapshot.settings)
        assertTrue(ok.warnings.isEmpty())
        assertEquals(BackupCodec.VERSION, ok.meta.version)
        assertEquals("2026-11-01T10:00:00Z", ok.meta.exportedAt)
        assertEquals("0.0.0.2", ok.meta.appVersion)
    }

    @Test
    fun `round trip keeps unicode quotes and the maximum length note`() {
        val ok = success(encode())
        assertEquals("Flat white ☕ \"oat\" / extra", ok.snapshot.transactions.first { it.id == 2L }.note)
        assertEquals(EntryLimits.NOTE_MAX, ok.snapshot.transactions.first { it.id == 5L }.note.length)
    }

    @Test
    fun `dangling cycle reference is accepted because entries outlive deleted cycles`() {
        val ok = success(encode())
        assertEquals(9L, ok.snapshot.transactions.first { it.id == 4L }.cycleId)
    }

    @Test
    fun `empty backup is valid`() {
        val ok = success(encode(BackupSnapshot(emptyList(), emptyList(), BackupSettings())))
        assertTrue(ok.snapshot.cycles.isEmpty() && ok.snapshot.transactions.isEmpty())
    }

    @Test
    fun `unknown extra fields are ignored for forward compatibility`() {
        val text = edit { put("futureField", JSONArray().put(1)); tx(0).put("futureTxField", "x") }
        assertEquals(2, success(text).snapshot.cycles.size)
    }

    // ───────────── rejection ─────────────

    @Test
    fun `plain text is rejected`() {
        assertTrue(failureReason("hello").contains("not valid JSON"))
    }

    @Test
    fun `another json document is rejected`() {
        assertTrue(failureReason("""{"name":"x"}""").contains("isn't a DVIDE backup"))
    }

    @Test
    fun `newer format version is rejected with an update hint`() {
        val reason = failureReason(edit { put("version", 2) })
        assertTrue(reason, reason.contains("newer version"))
    }

    @Test
    fun `missing or zero version is rejected`() {
        assertTrue(failureReason(edit { remove("version") }).contains("no format version"))
        assertTrue(failureReason(edit { put("version", 0) }).contains("invalid format version"))
    }

    @Test
    fun `missing lists are rejected`() {
        assertTrue(failureReason(edit { remove("cycles") }).contains("cycles"))
        assertTrue(failureReason(edit { remove("transactions") }).contains("entries"))
    }

    @Test
    fun `invalid date is rejected`() {
        assertTrue(failureReason(edit { tx(0).put("date", "2026-13-45") }).contains("valid date"))
    }

    @Test
    fun `cycle ending before it starts is rejected`() {
        assertTrue(failureReason(edit { cyc(0).put("endDate", "2026-07-30") }).contains("ends before"))
    }

    @Test
    fun `duplicate ids are rejected`() {
        assertTrue(failureReason(edit { cyc(1).put("id", 1) }).contains("repeats id"))
        assertTrue(failureReason(edit { tx(1).put("id", 1) }).contains("repeats id"))
    }

    @Test
    fun `unknown kind is rejected`() {
        assertTrue(failureReason(edit { tx(0).put("kind", "gift") }).contains("unknown kind"))
    }

    @Test
    fun `over long note and blank category are rejected rather than truncated`() {
        val long = "x".repeat(EntryLimits.NOTE_MAX + 1)
        assertTrue(failureReason(edit { tx(0).put("note", long) }).contains("note longer"))
        assertTrue(failureReason(edit { tx(0).put("category", "  ") }).contains("category"))
    }

    @Test
    fun `negative or absurd amounts are rejected`() {
        assertTrue(failureReason(edit { tx(0).put("amount", -5) }).contains("out of range"))
        assertTrue(failureReason(edit { tx(0).put("amount", 1.0e15) }).contains("out of range"))
    }

    @Test
    fun `wrong json types are rejected`() {
        assertTrue(failureReason(edit { tx(0).put("amount", "400") }).contains("must be a number"))
        assertTrue(failureReason(edit { cyc(0).put("month", "8") }).contains("whole number"))
    }

    @Test
    fun `color outside the int range is rejected`() {
        assertTrue(failureReason(edit { tx(3).put("color", 99_999_999_999L) }).contains("invalid color"))
    }

    @Test
    fun `one bad record rejects the whole file with no partial result`() {
        assertTrue(BackupCodec.decode(edit { tx(4).put("kind", "gift") }) is BackupParse.Failure)
    }

    // ───────────── lenient preferences ─────────────

    @Test
    fun `unknown preference values are skipped with a warning and never fail the restore`() {
        val text = edit { getJSONObject("settings").put("fontOption", "comic_sans").put("payday", 40) }
        val ok = success(text)
        assertNull(ok.snapshot.settings.fontOption)
        assertNull(ok.snapshot.settings.payday)
        assertEquals(2, ok.warnings.size)
        assertEquals(212, ok.snapshot.settings.seedHue) // untouched siblings survive
    }

    @Test
    fun `unsupported week start is skipped with exactly one warning`() {
        val ok = success(edit { getJSONObject("settings").put("weekStartDay", 4) })
        assertNull(ok.snapshot.settings.weekStartDay)
        assertEquals(1, ok.warnings.count { it.contains("weekStartDay") })
    }

    @Test
    fun `missing settings block restores data and warns`() {
        val ok = success("""{"format":"dvide-backup","version":1,"cycles":[],"transactions":[]}""")
        assertEquals(BackupSettings(), ok.snapshot.settings)
        assertEquals(1, ok.warnings.size)
    }
}
