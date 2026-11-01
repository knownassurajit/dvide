package com.knownassurajit.dvide_finance.app.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knownassurajit.dvide_finance.app.data.backup.BackupCounts
import com.knownassurajit.dvide_finance.app.data.backup.BackupParse
import com.knownassurajit.dvide_finance.app.data.backup.BackupRepository
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.data.repository.CycleRepository
import com.knownassurajit.dvide_finance.app.data.repository.SettingsRepository
import com.knownassurajit.dvide_finance.app.data.repository.TransactionRepository
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.data.profile.ProfileImages
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.domain.model.PastCycle
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.ui.entry.NewTransaction
import com.knownassurajit.dvide_finance.app.widget.DvideWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
    private val cycleRepo: CycleRepository,
    private val backupRepo: BackupRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private fun notifyWidget() {
        try {
            DvideWidgetProvider.updateAll(context)
        } catch (_: Exception) {}
    }

    // Today's date as state, so day counts ("days left", next salary) follow the calendar instead of being
    // frozen at the moment the last data change happened. Refreshed at midnight and whenever the app returns
    // to the foreground (a sleeping device can postpone the timer).
    private val _today = MutableStateFlow(LocalDate.now())
    private val today: LocalDate get() = _today.value

    fun refreshToday() {
        _today.value = LocalDate.now()
    }

    init {
        // Housekeeping: drop avatar files nobody references (e.g. a photo cropped but never saved).
        viewModelScope.launch {
            val ref = settingsRepo.settingsFlow.first().profilePictureUri
            ProfileImages.prune(context, keepRef = ref)
        }
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val untilMidnight = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay())
                delay(untilMidnight.toMillis() + 1_000L)
                refreshToday()
            }
        }
    }

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    // Room snapshots are nullable until the first query result arrives so "not loaded yet" is never confused
    // with "empty" (which would flash the first-cycle empty state on every cold start).
    private val cyclesSnapshot: StateFlow<List<ManualCycle>?> = cycleRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val transactionsSnapshot: StateFlow<List<Transaction>?> = transactionRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val transactions: StateFlow<List<Transaction>> = transactionsSnapshot
        .map { it.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val cycles: StateFlow<List<ManualCycle>> = cyclesSnapshot
        .map { it.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * True once settings, cycles and entries have each delivered their first value. MainActivity holds the first
     * frame until then, so the UI is drawn once, with the user's real theme and screen. All of these flows run on
     * the main dispatcher, so by the time the next frame is prepared the derived flows below are up to date too.
     */
    val isReady: StateFlow<Boolean> = combine(settings, cyclesSnapshot, transactionsSnapshot) { s, c, t ->
        s.isLoaded && c != null && t != null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentCycle: StateFlow<ManualCycle?> = combine(cycles, _today) { allCycles, day ->
        CycleEngine.resolveCurrentCycle(allCycles, day)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val metrics: StateFlow<Metrics?> = combine(currentCycle, transactions, _today) { cycle, txns, day ->
        cycle?.let { CycleEngine.computeMetrics(it, txns, day) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _highlightId = MutableStateFlow<Long?>(null)
    val highlightId: StateFlow<Long?> = _highlightId.asStateFlow()

    private val _viewIsWeekly = MutableStateFlow(false)
    val viewIsWeekly: StateFlow<Boolean> = _viewIsWeekly.asStateFlow()

    val pastCycles: StateFlow<List<PastCycle>> = combine(cycles, transactions, _today) { allCycles, txns, day ->
        CycleEngine.calculatePastCycles(
            cycles = allCycles,
            transactions = txns,
            today = day,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun completeOnboarding(
        name: String,
        email: String,
        currencyCode: String,
        regionCode: String,
        weekStartDay: Int,
        numberFormat: String,
        payday: Int,
        income: Double,
    ) {
        viewModelScope.launch {
            settingsRepo.setUserName(name)
            settingsRepo.setUserEmail(email)
            settingsRepo.setCurrencyCode(currencyCode)
            settingsRepo.setRegionCode(regionCode)
            settingsRepo.setWeekStartDay(weekStartDay)
            settingsRepo.setNumberFormat(numberFormat)
            settingsRepo.setPayday(payday)
            val (start, end) = CycleEngine.windowForPayday(payday, today)
            cycleRepo.insert(
                ManualCycle(
                    month = start.monthValue,
                    year = start.year,
                    startDate = start,
                    endDate = end,
                    income = income,
                )
            )
            settingsRepo.markSeeded()
            notifyWidget()
        }
    }

    fun addTransaction(newTx: NewTransaction) {
        viewModelScope.launch {
            val id = transactionRepo.insert(
                Transaction(
                    date     = newTx.date,
                    category = EntryLimits.normalize(newTx.category, EntryLimits.CATEGORY_MAX),
                    kind     = newTx.kind,
                    amount   = newTx.amount,
                    note     = EntryLimits.normalize(newTx.note, EntryLimits.NOTE_MAX),
                    color    = newTx.color,
                    cycleId  = newTx.cycleId ?: currentCycle.value?.id,
                )
            )
            _highlightId.value = id
            notifyWidget()
            delay(1_400)
            _highlightId.value = null
        }
    }

    fun addFundsToCurrentCycle(amount: Double, note: String = "Top-up balance") {
        val cycle = currentCycle.value ?: return
        viewModelScope.launch {
            val updated = cycle.copy(income = cycle.income + amount)
            cycleRepo.update(updated)
            val txId = transactionRepo.insert(
                Transaction(
                    date = today,
                    category = "income",
                    kind = "income",
                    amount = amount,
                    note = note.ifBlank { "Top-up balance" },
                    cycleId = cycle.id,
                )
            )
            _highlightId.value = txId
            notifyWidget()
            delay(1_400)
            _highlightId.value = null
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            transactionRepo.update(
                tx.copy(
                    category = EntryLimits.normalize(tx.category, EntryLimits.CATEGORY_MAX),
                    note = EntryLimits.normalize(tx.note, EntryLimits.NOTE_MAX),
                )
            )
            notifyWidget()
        }
    }

    fun setFontOption(font: AppFont) {
        viewModelScope.launch { settingsRepo.setFontOption(font) }
    }

    fun addCycle(cycle: ManualCycle) {
        viewModelScope.launch {
            cycleRepo.insert(cycle)
            notifyWidget()
        }
    }

    fun updateCycle(cycle: ManualCycle) {
        viewModelScope.launch {
            cycleRepo.update(cycle)
            notifyWidget()
        }
    }

    fun deleteCycle(cycle: ManualCycle) {
        viewModelScope.launch {
            cycleRepo.delete(cycle)
            notifyWidget()
        }
    }

    fun toggleTheme() {
        viewModelScope.launch { settingsRepo.setDarkTheme(!settings.value.darkTheme) }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { settingsRepo.setDarkTheme(dark) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setDynamicColor(enabled) }
    }

    fun setVariant(variant: DashboardVariant) {
        viewModelScope.launch { settingsRepo.setDashboardVariant(variant) }
    }

    fun setSeedHue(hue: Int) {
        viewModelScope.launch { settingsRepo.setSeedHue(hue) }
    }

    fun setViewIsWeekly(weekly: Boolean) {
        _viewIsWeekly.value = weekly
    }

    /**
     * Saves the local profile. [profilePhotoRef] is a file-name reference produced by [ProfileImages.saveAvatar]
     * (null removes the photo). Photos no longer referenced are deleted from private storage.
     */
    fun updateProfile(name: String, email: String, profilePhotoRef: String? = null) {
        viewModelScope.launch {
            settingsRepo.setUserName(EntryLimits.normalize(name, EntryLimits.PROFILE_NAME_MAX))
            settingsRepo.setUserEmail(EntryLimits.normalize(email, EntryLimits.PROFILE_EMAIL_MAX))
            if (profilePhotoRef != settings.value.profilePictureUri) {
                settingsRepo.setProfilePictureUri(profilePhotoRef)
            }
            ProfileImages.prune(context, keepRef = profilePhotoRef)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            transactionRepo.delete(tx)
            notifyWidget()
        }
    }

    fun setCurrencyCode(code: String) {
        viewModelScope.launch {
            settingsRepo.setCurrencyCode(code)
            notifyWidget()
        }
    }

    fun setRegionCode(code: String) {
        viewModelScope.launch {
            settingsRepo.setRegionCode(code)
            notifyWidget()
        }
    }

    fun setWeekStartDay(day: Int) {
        viewModelScope.launch { settingsRepo.setWeekStartDay(day) }
    }

    fun setNumberFormat(format: String) {
        viewModelScope.launch {
            settingsRepo.setNumberFormat(format)
            notifyWidget()
        }
    }

    fun setShowDecimals(show: Boolean) {
        viewModelScope.launch {
            settingsRepo.setShowDecimals(show)
            notifyWidget()
        }
    }

    fun setProfilePictureUri(uri: String?) {
        viewModelScope.launch { settingsRepo.setProfilePictureUri(uri) }
    }

    fun setPayday(day: Int) {
        viewModelScope.launch { settingsRepo.setPayday(day) }
    }

    fun setSwipeLeft(action: SwipeAction) {
        viewModelScope.launch { settingsRepo.setSwipeLeft(action) }
    }

    fun setSwipeRight(action: SwipeAction) {
        viewModelScope.launch { settingsRepo.setSwipeRight(action) }
    }

    suspend fun exportBackup(uri: Uri): Result<BackupCounts> = backupRepo.exportTo(uri)

    suspend fun readBackup(uri: Uri): BackupParse = backupRepo.read(uri)

    suspend fun restoreBackup(backup: BackupParse.Success): Result<BackupCounts> =
        backupRepo.restore(backup).also { if (it.isSuccess) notifyWidget() }

    fun metricsFor(cycleId: Long): Metrics? {
        val cycle = cycles.value.firstOrNull { it.id == cycleId } ?: return null
        return CycleEngine.computeMetrics(cycle, transactions.value, today)
    }

    fun cycleById(cycleId: Long): ManualCycle? = cycles.value.firstOrNull { it.id == cycleId }

    fun exportCsv(): String = CycleEngine.buildExportCsv(cycles.value, transactions.value)

    fun exportLedger(format: com.knownassurajit.dvide_finance.app.domain.model.ExportFormat): String =
        CycleEngine.buildExport(format, cycles.value, transactions.value)

    fun exportCycle(cycleId: Long, format: com.knownassurajit.dvide_finance.app.domain.model.ExportFormat = com.knownassurajit.dvide_finance.app.domain.model.ExportFormat.CSV): String? {
        val cycle = cycles.value.firstOrNull { it.id == cycleId } ?: return null
        return CycleEngine.buildCycleExport(format, cycle, transactions.value)
    }
}
