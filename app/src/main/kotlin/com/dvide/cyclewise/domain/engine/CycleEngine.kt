package com.dvide.app.domain.engine

import com.dvide.app.data.model.Category
import com.dvide.app.data.model.Transaction
import com.dvide.app.domain.model.Cycle
import com.dvide.app.domain.model.Metrics
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

object CycleEngine {

    fun cycleFor(today: LocalDate, anchorDay: Int): Cycle {
        val d = today.dayOfMonth
        var startMonth = today.monthValue - 1   // 0-indexed
        var startYear  = today.year

        val clampedThisMonth = clampDay(startYear, startMonth, anchorDay)
        if (d < clampedThisMonth) {
            startMonth -= 1
            if (startMonth < 0) { startMonth = 11; startYear -= 1 }
        }

        val start = LocalDate.of(startYear, startMonth + 1, clampDay(startYear, startMonth, anchorDay))

        var endMonth = startMonth + 1
        var endYear  = startYear
        if (endMonth > 11) { endMonth = 0; endYear += 1 }

        val nextAnchor = LocalDate.of(endYear, endMonth + 1, clampDay(endYear, endMonth, anchorDay))
        val end        = nextAnchor.minusDays(1)

        val totalDays = ChronoUnit.DAYS.between(start, nextAnchor).toInt()
        val dayIndex  = ChronoUnit.DAYS.between(start, today).toInt()
        val remaining = max(1, totalDays - dayIndex)
        val progress  = min(1f, max(0f, dayIndex.toFloat() / totalDays.toFloat()))

        return Cycle(start, end, totalDays, dayIndex, remaining, progress)
    }

    fun computeMetrics(
        income: Double,
        anchorDay: Int,
        transactions: List<Transaction>,
        today: LocalDate,
        cycleEnded: Boolean = false,
    ): Metrics {
        val cycle = cycleFor(today, anchorDay)
        val txns  = transactions
            .filter { inCycle(it.date, cycle) }
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })

        val byCategory = mutableMapOf<String, Double>()
        txns.forEach { tx -> byCategory[tx.category] = (byCategory[tx.category] ?: 0.0) + tx.amount }

        var allocated = 0.0
        var spent     = 0.0
        byCategory.forEach { (cat, amt) ->
            val kind = Category.kindOf(cat, txns.firstOrNull { it.category == cat }?.kind)
            if (kind == Category.Kind.ASIDE) allocated += amt else spent += amt
        }

        val spendable       = income - allocated
        val balance         = spendable - spent
        val safeToSpend     = balance / cycle.remaining

        val baseline        = spendable / cycle.totalDays
        val elapsed         = max(1, cycle.dayIndex + 1)
        val dailyVelocity   = spent / elapsed
        val projectedSpend  = dailyVelocity * cycle.totalDays
        val projectedClose  = spendable - projectedSpend

        val tight    = safeToSpend < baseline * 0.6
        val ended    = cycleEnded || today > cycle.end
        val surplus  = max(0.0, balance)
        val borrowed = max(0.0, -balance)

        return Metrics(
            cycle, txns, byCategory,
            income, allocated, spent, spendable, balance, safeToSpend,
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

    fun groupByWeek(txns: List<Transaction>): List<TransactionGroup> {
        return txns
            .groupBy { tx ->
                // ISO week starts Monday (day 1)
                val dow = (tx.date.dayOfWeek.value - 1) // Mon=0 .. Sun=6
                tx.date.minusDays(dow.toLong())
            }
            .entries
            .sortedByDescending { it.key }
            .map { (monday, items) ->
                val label = "WEEK OF ${monday.dayOfMonth} ${monday.month.name.take(3)}"
                TransactionGroup(monday, label, items, items.sumOf { it.amount })
            }
    }

    // Clamp anchor day to the actual last day of a given month (0-indexed month).
    private fun clampDay(year: Int, month0: Int, day: Int): Int {
        val m = ((month0 % 12) + 12) % 12
        val y = year + when {
            month0 < 0  -> (month0 - 11) / 12
            month0 > 11 -> month0 / 12
            else        -> 0
        }
        val lastDay = LocalDate.of(y, m + 1, 1).lengthOfMonth()
        return min(day, lastDay)
    }

    private fun inCycle(date: LocalDate, cycle: Cycle): Boolean =
        !date.isBefore(cycle.start) && !date.isAfter(cycle.end)
}
