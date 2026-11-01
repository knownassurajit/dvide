package com.knownassurajit.dvide_finance.app

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.knownassurajit.dvide_finance.app.data.backup.BackupCodec
import com.knownassurajit.dvide_finance.app.data.backup.BackupParse
import com.knownassurajit.dvide_finance.app.data.backup.BackupRepository
import com.knownassurajit.dvide_finance.app.data.local.DvideDatabase
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.repository.CycleRepository
import com.knownassurajit.dvide_finance.app.data.repository.SettingsRepository
import com.knownassurajit.dvide_finance.app.data.repository.TransactionRepository
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate

/**
 * Integration tests over a real DataStore file and an in-memory Room database (Robolectric).
 * They cover the pieces that decide whether user data survives: startup readiness, swipe preferences,
 * and a backup -> restore round trip through real files.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PersistenceIntegrationTest {

    @get:Rule val tmp = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var scope: CoroutineScope
    private val databases = mutableListOf<DvideDatabase>()

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @After fun tearDown() {
        databases.forEach { it.close() }
        scope.cancel()
    }

    private fun newDataStore(name: String = "settings"): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { File(tmp.root, "$name.preferences_pb") }

    private fun newDb(): DvideDatabase =
        Room.inMemoryDatabaseBuilder(context, DvideDatabase::class.java).allowMainThreadQueries().build()
            .also { databases += it }

    private class Env(
        val db: DvideDatabase,
        val settings: SettingsRepository,
        val backup: BackupRepository,
        val cycles: CycleRepository,
        val transactions: TransactionRepository,
    )

    private fun env(dataStoreName: String = "settings"): Env {
        val db = newDb()
        val settings = SettingsRepository(newDataStore(dataStoreName))
        return Env(
            db = db,
            settings = settings,
            backup = BackupRepository(context, db, db.cycleDao(), db.transactionDao(), settings),
            cycles = CycleRepository(db.cycleDao()),
            transactions = TransactionRepository(db.transactionDao()),
        )
    }

    private fun today(): LocalDate = LocalDate.now()

    private fun seed(e: Env): Pair<ManualCycle, Transaction> = runBlocking {
        val c = ManualCycle(1, today().monthValue, today().year, today().minusDays(5), today().plusDays(20), 3000.0)
        e.cycles.insert(c)
        val t = Transaction(1, today(), "essentials", "expense", 25.0, "Groceries", null, 1)
        e.transactions.insert(t)
        c to t
    }

    // ───────────── startup readiness ─────────────

    private fun newViewModel(e: Env) = MainViewModel(e.transactions, e.settings, e.cycles, e.backup, context)

    /** Drains the main looper until [done] (work happens on IO threads and posts back to main). */
    private fun pumpUntil(timeoutMs: Long = 10_000, done: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!done() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test fun `view model is not ready before data arrives and settings start unloaded`() {
        val vm = newViewModel(env())
        assertFalse("must not draw from placeholder defaults", vm.isReady.value)
        assertFalse(vm.settings.value.isLoaded)
        assertNull(vm.metrics.value)
    }

    @Test fun `by the time it reports ready the derived screen state is already populated`() {
        val e = env()
        seed(e)
        runBlocking { e.settings.markSeeded(); e.settings.setDarkTheme(false) }

        val vm = newViewModel(e)
        pumpUntil { vm.isReady.value }

        assertTrue(vm.isReady.value)
        // None of these may still hold placeholder values once ready, or the first frame would flash them.
        assertTrue(vm.settings.value.isLoaded)
        assertTrue("persisted seeded flag, not the default", vm.settings.value.seeded)
        assertFalse("persisted light theme, not the default dark", vm.settings.value.darkTheme)
        assertNotNull(vm.currentCycle.value)
        assertNotNull(vm.metrics.value)
        assertEquals(1, vm.transactions.value.size)
    }

    @Test fun `a fresh install becomes ready with no cycle so the empty state is legitimate`() {
        val vm = newViewModel(env())
        pumpUntil { vm.isReady.value }
        assertTrue(vm.isReady.value)
        assertFalse(vm.settings.value.seeded)
        assertNull(vm.currentCycle.value)
    }

    // ───────────── settings ─────────────

    @Test fun `swipe defaults are left edit and right delete`() = runBlocking {
        val s = env().settings.settingsFlow.first()
        assertEquals(SwipeAction.EDIT, s.swipeLeft)
        assertEquals(SwipeAction.DELETE, s.swipeRight)
        assertTrue(s.isLoaded)
    }

    @Test fun `swipe choices persist across a new repository on the same file`() = runBlocking {
        val file = File(tmp.root, "swipe.preferences_pb")

        // "First launch": write, then shut the store down completely (DataStore allows one live store per file).
        val firstScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val first = SettingsRepository(PreferenceDataStoreFactory.create(scope = firstScope) { file })
        first.setSwipeLeft(SwipeAction.NONE)
        first.setSwipeRight(SwipeAction.EDIT)
        firstScope.coroutineContext[kotlinx.coroutines.Job]!!.apply { cancel(); join() }

        // "Second launch": a brand new store over the same file sees the saved choices.
        val reopened = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
            .settingsFlow.first()
        assertEquals(SwipeAction.NONE, reopened.swipeLeft)
        assertEquals(SwipeAction.EDIT, reopened.swipeRight)
    }

    @Test fun `an unknown stored swipe key falls back to the default instead of breaking the list`() = runBlocking {
        val store = newDataStore("bad")
        val repo = SettingsRepository(store)
        store.updateData { p -> p.toMutablePreferences().also { it[SettingsRepository.KEY_SWIPE_LEFT] = "archive" } }
        assertEquals(SwipeAction.EDIT, repo.settingsFlow.first().swipeLeft)
    }

    // ───────────── backup / restore ─────────────

    @Test fun `backup then restore into a fresh install reproduces cycles entries and settings`() = runBlocking {
        val source = env("src")
        val (cycle, tx) = seed(source)
        val longNote = Transaction(2, today(), "coffee", "expense", 4.5, "x".repeat(200), 0xFFEA4335.toInt(), 1)
        source.transactions.insert(longNote)
        source.settings.setUserName("Sam"); source.settings.setSeedHue(212); source.settings.setDynamicColor(true)
        source.settings.setDashboardVariant(DashboardVariant.GAUGE); source.settings.setFontOption(AppFont.INTER)
        source.settings.setSwipeLeft(SwipeAction.DELETE); source.settings.setPayday(31)

        val file = File(tmp.root, "backup.json")
        val counts = source.backup.exportTo(Uri.fromFile(file)).getOrThrow()
        assertEquals(1, counts.cycles); assertEquals(2, counts.transactions)

        val target = env("dst")
        val parsed = target.backup.read(Uri.fromFile(file))
        assertTrue(parsed is BackupParse.Success)
        target.backup.restore(parsed as BackupParse.Success).getOrThrow()

        assertEquals(listOf(cycle), target.db.cycleDao().getAll())
        assertEquals(setOf(tx, longNote), target.db.transactionDao().getAll().toSet())
        val s = target.settings.settingsFlow.first()
        assertEquals("Sam", s.userName); assertEquals(212, s.seedHue); assertTrue(s.dynamicColor)
        assertEquals(DashboardVariant.GAUGE, s.dashboardVariant); assertEquals(AppFont.INTER, s.fontOption)
        assertEquals(SwipeAction.DELETE, s.swipeLeft); assertEquals(31, s.payday)
        assertTrue("restored app skips onboarding", s.seeded)
    }

    @Test fun `restore replaces existing data and keeps untouched settings`() = runBlocking {
        val e = env()
        e.cycles.insert(ManualCycle(7, 1, 2020, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 31), 1.0))
        e.transactions.insert(Transaction(50, LocalDate.of(2020, 1, 2), "old", "expense", 1.0, "old data", null, 7))
        e.settings.setCurrencyCode("JPY")

        val (cycle, tx) = seed(env("other"))
        val json = BackupCodec.encode(
            com.knownassurajit.dvide_finance.app.data.backup.BackupSnapshot(
                listOf(cycle), listOf(tx), com.knownassurajit.dvide_finance.app.data.backup.BackupSettings(userName = "Kim"),
            ),
            java.time.Instant.now(), "test",
        )
        e.backup.restore(BackupCodec.decode(json) as BackupParse.Success).getOrThrow()

        assertEquals(listOf(cycle), e.db.cycleDao().getAll())
        assertEquals(listOf(tx), e.db.transactionDao().getAll())
        val s = e.settings.settingsFlow.first()
        assertEquals("Kim", s.userName)
        assertEquals("a null field in the backup leaves the device value alone", "JPY", s.currencyCode)
    }

    @Test fun `new cycles never reuse an id that restored entries still point at`() = runBlocking {
        // Entry points at cycle 9, which no longer exists (deleting a cycle keeps its entries).
        val orphan = Transaction(1, today(), "essentials", "expense", 5.0, "kept after cycle delete", null, 9)
        val c1 = ManualCycle(1, 1, 2026, today().minusDays(2), today().plusDays(2), 100.0)
        val e = env()
        val json = BackupCodec.encode(
            com.knownassurajit.dvide_finance.app.data.backup.BackupSnapshot(
                listOf(c1), listOf(orphan), com.knownassurajit.dvide_finance.app.data.backup.BackupSettings(),
            ),
            java.time.Instant.now(), "test",
        )
        e.backup.restore(BackupCodec.decode(json) as BackupParse.Success).getOrThrow()

        val newId = e.cycles.insert(ManualCycle(0, 2, 2026, today().plusDays(3), today().plusDays(30), 100.0))
        assertTrue("new cycle id $newId must not collide with the orphan's cycle 9", newId > 9)
    }

    @Test fun `reading a non backup file reports a clear failure and changes nothing`() = runBlocking {
        val e = env()
        seed(e)
        val junk = File(tmp.root, "junk.json").apply { writeText("{ not json") }
        val parsed = e.backup.read(Uri.fromFile(junk))
        assertTrue(parsed is BackupParse.Failure)
        assertEquals(1, e.db.cycleDao().getAll().size)
    }

    @Test fun `reading a missing file reports a failure instead of crashing`() = runBlocking {
        val parsed = env().backup.read(Uri.fromFile(File(tmp.root, "does-not-exist.json")))
        assertTrue(parsed is BackupParse.Failure)
    }
}
