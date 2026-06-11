package com.dvide.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dvide.app.data.model.Transaction
import com.dvide.app.data.repository.AppSettings
import com.dvide.app.data.repository.SettingsRepository
import com.dvide.app.data.repository.TransactionRepository
import com.dvide.app.domain.engine.CycleEngine
import com.dvide.app.domain.model.DashboardVariant
import com.dvide.app.domain.model.Metrics
import com.dvide.app.domain.model.PastCycle
import com.dvide.app.ui.entry.NewTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now()

    // ── Settings ──────────────────────────────────────────────────────────
    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    // ── Transactions ──────────────────────────────────────────────────────
    private val transactions: StateFlow<List<Transaction>> = transactionRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Computed metrics ──────────────────────────────────────────────────
    val metrics: StateFlow<Metrics> = combine(settings, transactions) { s, txns ->
        CycleEngine.computeMetrics(
            income      = s.income,
            anchorDay   = s.anchorDay,
            transactions = txns,
            today       = today,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CycleEngine.computeMetrics(3_200.0, 25, emptyList(), LocalDate.now()),
    )

    // ── UI state ──────────────────────────────────────────────────────────
    private val _highlightId = MutableStateFlow<Long?>(null)
    val highlightId: StateFlow<Long?> = _highlightId.asStateFlow()

    private val _viewIsWeekly = MutableStateFlow(false)
    val viewIsWeekly: StateFlow<Boolean> = _viewIsWeekly.asStateFlow()

    // ── Dynamic past cycles (archive) ────────────────────────────────────
    val pastCycles: StateFlow<List<PastCycle>> = combine(settings, transactions) { s, txns ->
        CycleEngine.calculatePastCycles(
            income       = s.income,
            anchorDay    = s.anchorDay,
            transactions = txns,
            today        = today,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Init: check first launch settings ─────────────────────────────────
    init {
        // No auto-seeding of mock data to ensure a clean, user-driven workspace
    }

    fun completeOnboarding(
        name: String,
        email: String,
        income: Double,
        anchorDay: Int,
        currencyCode: String,
        regionCode: String,
        weekStartDay: Int,
        numberFormat: String
    ) {
        viewModelScope.launch {
            settingsRepo.setUserName(name)
            settingsRepo.setUserEmail(email)
            settingsRepo.setIncome(income)
            settingsRepo.setAnchorDay(anchorDay)
            settingsRepo.setCurrencyCode(currencyCode)
            settingsRepo.setRegionCode(regionCode)
            settingsRepo.setWeekStartDay(weekStartDay)
            settingsRepo.setNumberFormat(numberFormat)
            settingsRepo.markSeeded()
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────

    fun addTransaction(newTx: NewTransaction) {
        viewModelScope.launch {
            val id = transactionRepo.insert(
                Transaction(
                    date     = today,
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

    fun toggleTheme() {
        viewModelScope.launch { settingsRepo.setDarkTheme(!settings.value.darkTheme) }
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

    fun setIncome(income: Double) {
        viewModelScope.launch { settingsRepo.setIncome(income) }
    }

    fun setAnchorDay(day: Int) {
        viewModelScope.launch { settingsRepo.setAnchorDay(day.coerceIn(1, 28)) }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            settingsRepo.setUserName(name)
            settingsRepo.setUserEmail(email)
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            transactionRepo.delete(tx)
        }
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
}

