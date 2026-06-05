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

    // ── Mock archive (past closed cycles) ────────────────────────────────
    val archiveMock: List<PastCycle> = listOf(
        PastCycle("May 2026",      "25 Apr – 24 May",  312.0),
        PastCycle("April 2026",    "25 Mar – 24 Apr",   86.0),
        PastCycle("March 2026",    "25 Feb – 24 Mar",  -47.0),
        PastCycle("February 2026", "25 Jan – 24 Feb",  204.0),
    )

    // ── Init: seed demo data on first launch ──────────────────────────────
    init {
        viewModelScope.launch {
            val firstSettings = settingsRepo.settingsFlow.first()
            if (!firstSettings.seeded) {
                seedDemoData()
                settingsRepo.markSeeded()
            }
        }
    }

    private suspend fun seedDemoData() {
        val cycle = CycleEngine.cycleFor(today, 25)
        val s     = cycle.start
        transactionRepo.insertAll(listOf(
            Transaction(date = s,               category = "savings",    kind = "aside",   amount = 400.0, note = "Emergency fund"),
            Transaction(date = s,               category = "investment", kind = "aside",   amount = 300.0, note = "Index fund · SIP"),
            Transaction(date = s.plusDays(1),   category = "security",   kind = "aside",   amount = 150.0, note = "Health insurance"),
            Transaction(date = s.plusDays(1),   category = "essentials", kind = "expense", amount = 48.20, note = "Groceries · Sainsbury's"),
            Transaction(date = s.plusDays(3),   category = "lifestyle",  kind = "expense", amount = 34.00, note = "Dinner out"),
            Transaction(date = s.plusDays(7),   category = "essentials", kind = "expense", amount = 18.40, note = "Pharmacy"),
            Transaction(date = s.plusDays(10),  category = "lifestyle",  kind = "expense", amount = 4.80,  note = "Flat white"),
        ))
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
}
