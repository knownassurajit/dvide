package com.knownassurajit.dvide_finance.app.domain.model

import com.knownassurajit.dvide_finance.app.data.model.Transaction

data class Metrics(
    val cycle: Cycle,
    val transactions: List<Transaction>,
    val byCategory: Map<String, Double>,
    val income: Double,
    val allocated: Double,
    val spent: Double,
    val spendable: Double,
    val balance: Double,
    val safeToSpend: Double,
    val baseline: Double,
    val dailyVelocity: Double,
    val projectedSpend: Double,
    val projectedClose: Double,
    val tight: Boolean,
    val ended: Boolean,
    val surplus: Double,
    val borrowed: Double,
    val sourceCycleId: Long = 0,
) {
    val balanceFraction: Float
        get() = if (spendable > 0) (balance / spendable).toFloat().coerceIn(0f, 1f) else 0f

    /** "12 days left", "1 day left", or "Cycle ended" once the window is over. */
    val daysLeftText: String
        get() = if (ended) "Cycle ended" else "${Cycle.daysText(cycle.remaining)} left"

    /** Compact form for tight spaces (gauge, widget): "12d left" / "Ended". */
    val daysLeftShort: String
        get() = if (ended) "Ended" else "${cycle.remaining}d left"
}
