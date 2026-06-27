package com.knownassurajit.dvide_finance.app.domain.engine

import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.domain.model.Cycle
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

object CycleEngine {

    fun computeMetrics(
        cycle: ManualCycle,
        transactions: List<Transaction>,
        today: LocalDate,
    ): Metrics {
        val totalDays = ChronoUnit.DAYS.between(cycle.startDate, cycle.endDate).toInt() + 1

        val dayIndex = if (today.isBefore(cycle.startDate)) {
            0
        } else if (today.isAfter(cycle.endDate)) {
            totalDays - 1
        } else {
             ChronoUnit.DAYS.between(cycle.startDate, today).toInt()
        }

        val remaining = max(1, totalDays - dayIndex)
        val progress  = min(1f, max(0f, dayIndex.toFloat() / totalDays.toFloat()))

        val domainCycle = Cycle(cycle.startDate, cycle.endDate, totalDays, dayIndex, remaining, progress)

        val cycleEnded = today.isAfter(cycle.endDate)

        val txns  = transactions
            .filter { inCycle(it.date, domainCycle) }
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })

        val byCategory = mutableMapOf<String, Double>()
        txns.forEach { tx -> byCategory[tx.category] = (byCategory[tx.category] ?: 0.0) + tx.amount }

        var allocated = 0.0
        var spent     = 0.0
        byCategory.forEach { (cat, amt) ->
            val kind = Category.kindOf(cat, txns.firstOrNull { it.category == cat }?.kind)
            if (kind == Category.Kind.ASIDE) allocated += amt else spent += amt
        }

        val spendable       = cycle.income - allocated
        val balance         = spendable - spent
        val safeToSpend     = balance / domainCycle.remaining

        val baseline        = spendable / domainCycle.totalDays
        val elapsed         = max(1, domainCycle.dayIndex + 1)
        val dailyVelocity   = spent / elapsed
        val projectedSpend  = dailyVelocity * domainCycle.totalDays
        val projectedClose  = spendable - projectedSpend

        val tight    = safeToSpend < baseline * 0.6
        val ended    = cycleEnded
        val surplus  = max(0.0, balance)
        val borrowed = max(0.0, -balance)

        return Metrics(
            domainCycle, txns, byCategory,
            cycle.income, allocated, spent, spendable, balance, safeToSpend,
            baseline, dailyVelocity, projectedSpend, projectedClose,
            tight, ended, surplus, borrowed,
        )
    }

    fun groupByDay(txns: List<Transaction>, today: LocalDate): List<TransactionGroup> {
        val yesterday = today.minusDays(1)
        return txns
            .groupBy { it.date }
            .entries
            .sortedByDescending { it.key }
            .map { (date, items) ->
                val label = when (date) {
                    today     -> "TODAY"
                    yesterday -> "YESTERDAY"
                    else      -> "${date.dayOfWeek.name.take(3)}, ${date.dayOfMonth} ${date.month.name.take(3)}"
                }
                TransactionGroup(date, label, items, items.sumOf { it.amount })
            }
    }

    fun groupByWeek(txns: List<Transaction>, weekStartDay: Int = 2): List<TransactionGroup> {
        val startDow = when (weekStartDay) {
            1 -> java.time.DayOfWeek.SUNDAY
            7 -> java.time.DayOfWeek.SATURDAY
            else -> java.time.DayOfWeek.MONDAY
        }
        return txns
            .groupBy { tx ->
                var d = tx.date
                while (d.dayOfWeek != startDow) {
                    d = d.minusDays(1)
                }
                d
            }
            .entries
            .sortedByDescending { it.key }
            .map { (startOfWeek, items) ->
                val label = "WEEK OF ${startOfWeek.dayOfMonth} ${startOfWeek.month.name.take(3)}"
                TransactionGroup(startOfWeek, label, items, items.sumOf { it.amount })
            }
    }

    fun calculatePastCycles(
        cycles: List<ManualCycle>,
        transactions: List<Transaction>,
        today: LocalDate
    ): List<com.knownassurajit.dvide_finance.app.domain.model.PastCycle> {
        if (transactions.isEmpty() || cycles.isEmpty()) return emptyList()

        val pastCycles = mutableListOf<com.knownassurajit.dvide_finance.app.domain.model.PastCycle>()

        cycles.forEach { cycle ->
            val m = computeMetrics(cycle, transactions, cycle.endDate)

            // Capitalize month name helper
            val rawMonth = cycle.endDate.month.name.lowercase()
            val monthLabel = rawMonth.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } + " ${cycle.endDate.year}"

            val rawStartMonth = cycle.startDate.month.name.take(3).lowercase()
            val startMonthLabel = rawStartMonth.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            val rawEndMonth = cycle.endDate.month.name.take(3).lowercase()
            val endMonthLabel = rawEndMonth.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

            val rangeLabel = "${cycle.startDate.dayOfMonth} $startMonthLabel – ${cycle.endDate.dayOfMonth} $endMonthLabel"

            pastCycles.add(com.knownassurajit.dvide_finance.app.domain.model.PastCycle(monthLabel, rangeLabel, m.balance))
        }

        return pastCycles
    }


    private fun inCycle(date: LocalDate, cycle: Cycle): Boolean =
        !date.isBefore(cycle.start) && !date.isAfter(cycle.end)
}
