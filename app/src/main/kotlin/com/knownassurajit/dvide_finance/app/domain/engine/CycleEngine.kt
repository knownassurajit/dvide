package com.knownassurajit.dvide_finance.app.domain.engine

import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.Cycle
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import com.knownassurajit.dvide_finance.app.domain.model.PastCycle
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

object CycleEngine {

    fun computeMetrics(
        cycle: ManualCycle,
        transactions: List<Transaction>,
        today: LocalDate,
    ): Metrics {
        val totalDays = max(1, ChronoUnit.DAYS.between(cycle.startDate, cycle.endDate).toInt() + 1)

        val dayIndex = when {
            today.isBefore(cycle.startDate) -> 0
            today.isAfter(cycle.endDate) -> totalDays - 1
            else -> ChronoUnit.DAYS.between(cycle.startDate, today).toInt().coerceIn(0, totalDays - 1)
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

        val spendable      = cycle.income - allocated
        val balance        = spendable - spent
        val safeToSpend    = balance / remaining
        val baseline       = spendable / totalDays
        val elapsed        = max(1, dayIndex + 1)
        val dailyVelocity  = spent / elapsed
        val projectedSpend = dailyVelocity * totalDays
        val projectedClose = spendable - projectedSpend
        val tight          = safeToSpend < baseline * 0.6
        val surplus        = max(0.0, balance)
        val borrowed       = max(0.0, -balance)

        return Metrics(
            cycle = domainCycle,
            transactions = txns,
            byCategory = byCategory,
            income = cycle.income,
            allocated = allocated,
            spent = spent,
            spendable = spendable,
            balance = balance,
            safeToSpend = safeToSpend,
            baseline = baseline,
            dailyVelocity = dailyVelocity,
            projectedSpend = projectedSpend,
            projectedClose = projectedClose,
            tight = tight,
            ended = cycleEnded,
            surplus = surplus,
            borrowed = borrowed,
            sourceCycleId = cycle.id,
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
        today: LocalDate,
    ): List<PastCycle> {
        if (cycles.isEmpty()) return emptyList()

        return cycles
            .filter { it.endDate.isBefore(today) }
            .sortedByDescending { it.endDate }
            .map { cycle ->
                val m = computeMetrics(cycle, transactions, cycle.endDate)
                val rawMonth = cycle.endDate.month.name.lowercase()
                val monthLabel = rawMonth.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                } + " ${cycle.endDate.year}"

                val startMonthLabel = cycle.startDate.month.name.take(3).lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                }
                val endMonthLabel = cycle.endDate.month.name.take(3).lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                }
                val rangeLabel = "${cycle.startDate.dayOfMonth} $startMonthLabel – ${cycle.endDate.dayOfMonth} $endMonthLabel"

                PastCycle(
                    cycleId = cycle.id,
                    label = monthLabel,
                    range = rangeLabel,
                    balance = m.balance,
                    income = cycle.income,
                )
            }
    }

    /**
     * Active window covering [today], else the next upcoming cycle, else the most recently ended.
     */
    fun resolveCurrentCycle(cycles: List<ManualCycle>, today: LocalDate): ManualCycle? {
        if (cycles.isEmpty()) return null
        cycles.firstOrNull { !today.isBefore(it.startDate) && !today.isAfter(it.endDate) }?.let { return it }
        cycles.filter { it.startDate.isAfter(today) }.minByOrNull { it.startDate }?.let { return it }
        return cycles.maxByOrNull { it.endDate }
    }

    fun paydayOn(year: Int, month: Int, payday: Int): LocalDate {
        val ym = YearMonth.of(year, month)
        val day = payday.coerceIn(1, 31).coerceAtMost(ym.lengthOfMonth())
        return ym.atDay(day)
    }

    /**
     * Salary window anchored to [payday]: from that day's pay date through the day before the next.
     */
    fun windowForPayday(payday: Int, today: LocalDate): Pair<LocalDate, LocalDate> {
        val thisPay = paydayOn(today.year, today.monthValue, payday)
        val start = if (!today.isBefore(thisPay)) {
            thisPay
        } else {
            val prev = today.minusMonths(1)
            paydayOn(prev.year, prev.monthValue, payday)
        }
        val nextMonth = start.plusMonths(1)
        val nextPay = paydayOn(nextMonth.year, nextMonth.monthValue, payday)
        return start to nextPay.minusDays(1)
    }

    fun suggestedNextWindow(
        existing: List<ManualCycle>,
        payday: Int,
        today: LocalDate,
    ): Pair<LocalDate, LocalDate> {
        val lastEnd = existing.maxByOrNull { it.endDate }?.endDate
        if (lastEnd != null) {
            val start = lastEnd.plusDays(1)
            val nextPay = paydayOn(start.plusMonths(1).year, start.plusMonths(1).monthValue, payday)
            val end = if (nextPay.isAfter(start)) nextPay.minusDays(1) else start.plusMonths(1).minusDays(1)
            return start to end
        }
        return windowForPayday(payday, today)
    }

    fun cyclesOverlap(start: LocalDate, end: LocalDate, existing: List<ManualCycle>, ignoreId: Long? = null): Boolean {
        return existing.any { cycle ->
            if (ignoreId != null && cycle.id == ignoreId) return@any false
            start.isBefore(cycle.endDate.plusDays(1)) && end.isAfter(cycle.startDate.minusDays(1))
        }
    }

    fun buildExportCsv(cycles: List<ManualCycle>, transactions: List<Transaction>): String {
        val header = "date,category,kind,amount,note,cycle"
        val rows = transactions.sortedWith(compareBy({ it.date }, { it.id })).map { tx ->
            val cycleLabel = cycles.firstOrNull { !tx.date.isBefore(it.startDate) && !tx.date.isAfter(it.endDate) }
                ?.let { "${it.startDate}..${it.endDate}" }
                ?: ""
            val note = tx.note.replace("\"", "\"\"")
            "${tx.date},${tx.category},${tx.kind},${"%.2f".format(java.util.Locale.US, tx.amount)},\"$note\",$cycleLabel"
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    private fun inCycle(date: LocalDate, cycle: Cycle): Boolean =
        !date.isBefore(cycle.start) && !date.isAfter(cycle.end)
}
