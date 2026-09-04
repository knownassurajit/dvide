package com.knownassurajit.dvide_finance.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.data.repository.CycleRepository
import com.knownassurajit.dvide_finance.app.data.repository.SettingsRepository
import com.knownassurajit.dvide_finance.app.data.repository.TransactionRepository
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.domain.model.PastCycle
import com.knownassurajit.dvide_finance.app.ui.entry.NewTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
    private val cycleRepo: CycleRepository,
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val transactions: StateFlow<List<Transaction>> = transactionRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cycles: StateFlow<List<ManualCycle>> = cycleRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentCycle: StateFlow<ManualCycle?> = cycles
        .combine(settings) { allCycles, _ ->
            CycleEngine.resolveCurrentCycle(allCycles, today)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val metrics: StateFlow<Metrics?> = combine(currentCycle, transactions) { cycle, txns ->
        cycle?.let { CycleEngine.computeMetrics(it, txns, today) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _highlightId = MutableStateFlow<Long?>(null)
    val highlightId: StateFlow<Long?> = _highlightId.asStateFlow()

    private val _viewIsWeekly = MutableStateFlow(false)
    val viewIsWeekly: StateFlow<Boolean> = _viewIsWeekly.asStateFlow()

    val pastCycles: StateFlow<List<PastCycle>> = combine(cycles, transactions) { allCycles, txns ->
        CycleEngine.calculatePastCycles(
            cycles = allCycles,
            transactions = txns,
            today = today,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
        }
    }

    fun addTransaction(newTx: NewTransaction) {
        viewModelScope.launch {
            val id = transactionRepo.insert(
                Transaction(
                    date     = newTx.date,
                    category = newTx.category,
                    kind     = newTx.kind,
                    amount   = newTx.amount,
                    note     = newTx.note,
                )
            )
            _highlightId.value = id
            delay(1_400)
            _highlightId.value = null
        }
    }

    fun addCycle(cycle: ManualCycle) {
        viewModelScope.launch { cycleRepo.insert(cycle) }
    }

    fun updateCycle(cycle: ManualCycle) {
        viewModelScope.launch { cycleRepo.update(cycle) }
    }

    fun deleteCycle(cycle: ManualCycle) {
        viewModelScope.launch { cycleRepo.delete(cycle) }
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

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            settingsRepo.setUserName(name)
            settingsRepo.setUserEmail(email)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { transactionRepo.delete(tx) }
    }

    fun setCurrencyCode(code: String) {
        viewModelScope.launch { settingsRepo.setCurrencyCode(code) }
    }

    fun setRegionCode(code: String) {
        viewModelScope.launch { settingsRepo.setRegionCode(code) }
    }

    fun setWeekStartDay(day: Int) {
        viewModelScope.launch { settingsRepo.setWeekStartDay(day) }
    }

    fun setNumberFormat(format: String) {
        viewModelScope.launch { settingsRepo.setNumberFormat(format) }
    }

    fun setPayday(day: Int) {
        viewModelScope.launch { settingsRepo.setPayday(day) }
    }

    fun metricsFor(cycleId: Long): Metrics? {
        val cycle = cycles.value.firstOrNull { it.id == cycleId } ?: return null
        return CycleEngine.computeMetrics(cycle, transactions.value, today)
    }

    fun cycleById(cycleId: Long): ManualCycle? = cycles.value.firstOrNull { it.id == cycleId }

    fun exportCsv(): String = CycleEngine.buildExportCsv(cycles.value, transactions.value)
}
