package com.knownassurajit.dvide_finance.app.domain.engine

import com.knownassurajit.dvide_finance.app.data.model.Transaction
import java.time.LocalDate

data class TransactionGroup(
    val date: LocalDate,
    val label: String,
    val items: List<Transaction>,
    val total: Double,
)
