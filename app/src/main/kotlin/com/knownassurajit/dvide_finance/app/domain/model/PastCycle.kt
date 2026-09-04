package com.knownassurajit.dvide_finance.app.domain.model

data class PastCycle(
    val cycleId: Long,
    val label: String,
    val range: String,
    val balance: Double,
    val income: Double = 0.0,
)
