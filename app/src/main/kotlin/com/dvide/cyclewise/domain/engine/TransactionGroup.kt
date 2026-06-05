package com.dvide.app.domain.engine

import com.dvide.app.data.model.Transaction
import java.time.LocalDate

data class TransactionGroup(
    val date: LocalDate,
    val label: String,
    val items: List<Transaction>,
    val total: Double,
)
