package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.util.formatMoney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleEngineTest {

    // ─────────────────────────────────────────────────────────────
    // computeMetrics — financial waterfall
    // ─────────────────────────────────────────────────────────────

    private fun tx(date: LocalDate, cat: String, kind: String, amount: Double) = Transaction(
        id = 0, date = date, category = cat, kind = kind, amount = amount, note = ""
    )

    private fun mc(startDate: LocalDate, endDate: LocalDate, income: Double) = ManualCycle(
        id = 0, month = startDate.monthValue, year = startDate.year, startDate = startDate, endDate = endDate, income = income
    )

    @Test
    fun `computeMetrics healthy scenario produces positive balance`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)

        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val txns = listOf(
            tx(cycleStart,               "savings",    "aside",   400.0),
            tx(cycleStart,               "investment", "aside",   300.0),
            tx(cycleStart.plusDays(1),   "security",   "aside",   150.0),
            tx(cycleStart.plusDays(1),   "essentials", "expense",  48.20),
            tx(cycleStart.plusDays(3),   "lifestyle",  "expense",  34.00),
            tx(cycleStart.plusDays(7),   "essentials", "expense",  18.40),
            tx(cycleStart.plusDays(10),  "lifestyle",  "expense",   4.80),
        )

        val m = CycleEngine.computeMetrics(cycle, txns, today)

        assertEquals(3200.0, m.income, 0.001)
        assertEquals(850.0,  m.allocated, 0.001)    // 400 + 300 + 150
        assertEquals(2350.0, m.spendable, 0.001)    // 3200 - 850
        assertEquals(105.40, m.spent, 0.001)        // 48.20 + 34 + 18.40 + 4.80
        assertEquals(2244.60, m.balance, 0.01)      // 2350 - 105.40
        assertTrue(m.balance > 0)
        assertFalse(m.tight)
        assertFalse(m.ended)
    }

    @Test
    fun `computeMetrics tight scenario sets tight flag`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)

        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val txns = listOf(
            tx(cycleStart, "savings",    "aside",  200.0),
            tx(cycleStart, "security",   "aside",  150.0),
            tx(cycleStart, "essentials", "expense", 1450.0),  // rent
            tx(cycleStart.plusDays(1),   "essentials", "expense", 96.30),
            tx(cycleStart.plusDays(2),   "lifestyle",  "expense", 180.00),
            tx(cycleStart.plusDays(4),   "lifestyle",  "expense", 240.00),
            tx(cycleStart.plusDays(6),   "essentials", "expense",  72.10),
            tx(cycleStart.plusDays(8),   "lifestyle",  "expense",  88.00),
            tx(cycleStart.plusDays(10),  "lifestyle",  "expense",  26.00),
        )

        val m = CycleEngine.computeMetrics(cycle, txns, today)

        assertTrue("Expected tight=true for over-spent scenario", m.tight)
        assertTrue(m.balance < m.spendable)
    }

    @Test
    fun `computeMetrics overspent scenario produces negative balance`() {
        val today = LocalDate.of(2026, 6, 15)
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)

        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val txns = listOf(
            tx(cycleStart,               "essentials", "expense", 2800.0),
            tx(cycleStart.plusDays(5),   "lifestyle",  "expense",  600.0),
            tx(cycleStart.plusDays(10),  "lifestyle",  "expense",  400.0),
        )

        val m = CycleEngine.computeMetrics(cycle, txns, today)

        assertTrue(m.balance < 0)
        assertTrue(m.borrowed > 0)
        assertEquals(0.0, m.surplus, 0.001)
    }

    @Test
    fun `computeMetrics ignores transactions outside the cycle`() {
        val today      = LocalDate.of(2026, 6, 4)
        val beforeCycle = LocalDate.of(2026, 4, 10)  // outside window
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)

        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val txns = listOf(
            tx(beforeCycle, "essentials", "expense", 500.0),
        )

        val m = CycleEngine.computeMetrics(cycle, txns, today)

        assertEquals(0.0, m.spent, 0.001)
        assertEquals(3200.0, m.balance, 0.001)
    }

    @Test
    fun `safeToSpend is balance divided by remaining days`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)
        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val m     = CycleEngine.computeMetrics(cycle, emptyList(), today)

        val expected = m.balance / m.cycle.remaining
        assertEquals(expected, m.safeToSpend, 0.001)
    }

    // ─────────────────────────────────────────────────────────────
    // groupByDay — timeline grouping
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `groupByDay labels today and yesterday correctly`() {
        val today     = LocalDate.of(2026, 6, 4)
        val yesterday = today.minusDays(1)

        val txns = listOf(
            tx(today,     "lifestyle",  "expense", 10.0),
            tx(yesterday, "essentials", "expense", 20.0),
        )

        val groups = CycleEngine.groupByDay(txns, today)

        assertEquals(2, groups.size)
        assertEquals("TODAY",     groups[0].label)
        assertEquals("YESTERDAY", groups[1].label)
    }

    @Test
    fun `groupByDay totals match sum of transactions per day`() {
        val today = LocalDate.of(2026, 6, 4)

        val txns = listOf(
            tx(today, "lifestyle",  "expense", 10.0),
            tx(today, "essentials", "expense", 20.0),
        )

        val groups = CycleEngine.groupByDay(txns, today)

        assertEquals(1, groups.size)
        assertEquals(30.0, groups[0].total, 0.001)
        assertEquals(2, groups[0].items.size)
    }

    @Test
    fun `groupByDay is sorted newest first`() {
        val today = LocalDate.of(2026, 6, 4)

        val txns = listOf(
            tx(today.minusDays(2), "lifestyle",  "expense", 10.0),
            tx(today,              "essentials", "expense", 20.0),
            tx(today.minusDays(1), "savings",    "aside",   30.0),
        )

        val groups = CycleEngine.groupByDay(txns, today)

        assertEquals(3, groups.size)
        assertEquals(today,              groups[0].date)
        assertEquals(today.minusDays(1), groups[1].date)
        assertEquals(today.minusDays(2), groups[2].date)
    }

    // ─────────────────────────────────────────────────────────────
    // Dynamic Locale Money Formatting
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `formatMoney formats correctly based on currency and region`() {
        val amount = 1250.50
        val ukFormatted2 = amount.formatMoney(currencyCode = "GBP", regionCode = "GB", decimals = 2)
        assertTrue(ukFormatted2.contains("£"))
        assertTrue(ukFormatted2.contains("1,250.50"))

        val usFormatted = amount.formatMoney(currencyCode = "USD", regionCode = "US", decimals = 2)
        assertTrue(usFormatted.contains("$"))
        assertTrue(usFormatted.contains("1,250.50"))
    }

    @Test
    fun `formatMoney applies custom number format overrides`() {
        val amount = 1250.50
        val customFormatted = amount.formatMoney(currencyCode = "USD", regionCode = "US", numberFormatOption = "COMMA_DECIMAL", decimals = 2)
        println("customFormatted = $customFormatted")
        assertTrue(customFormatted.contains("1.250,50"))
        
        val spaceFormatted = amount.formatMoney(currencyCode = "USD", regionCode = "US", numberFormatOption = "SPACE_DECIMAL", decimals = 2)
        println("spaceFormatted = $spaceFormatted")
        assertTrue(spaceFormatted.contains("1 250,50"))
    }

    // ─────────────────────────────────────────────────────────────
    // Dynamic Week Start Grouping
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `groupByWeek groups according to weekStartDay setting`() {
        val thu = LocalDate.of(2026, 6, 4)
        val satBefore = LocalDate.of(2026, 5, 30)
        val monBefore = LocalDate.of(2026, 6, 1)

        val txns = listOf(
            tx(thu, "lifestyle", "expense", 50.0)
        )

        val monGroup = CycleEngine.groupByWeek(txns, weekStartDay = 2)
        assertEquals(1, monGroup.size)
        assertEquals(monBefore, monGroup[0].date)

        val satGroup = CycleEngine.groupByWeek(txns, weekStartDay = 7)
        assertEquals(1, satGroup.size)
        assertEquals(satBefore, satGroup[0].date)
    }

    // ─────────────────────────────────────────────────────────────
    // Dynamic Past Cycle Calculator
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `calculatePastCycles dynamically derives correct archive lists`() {
        val today = LocalDate.of(2026, 6, 28)
        val mayTx = tx(LocalDate.of(2026, 6, 10), "lifestyle", "expense", 100.0)
        val juneTx = tx(LocalDate.of(2026, 6, 26), "lifestyle", "expense", 20.0)
        
        val cycleStart = LocalDate.of(2026, 5, 25)
        val cycleEnd = LocalDate.of(2026, 6, 24)
        val cycle = mc(cycleStart, cycleEnd, 3200.0)

        val txns = listOf(mayTx, juneTx)
        val past = CycleEngine.calculatePastCycles(listOf(cycle), transactions = txns, today = today)
        
        assertEquals(1, past.size)
        assertEquals(0L, past[0].cycleId)
        assertEquals("June 2026", past[0].label)
        assertEquals("25 May – 24 Jun", past[0].range)
        assertEquals(3100.0, past[0].balance, 0.001)
    }

    @Test
    fun `calculatePastCycles excludes the active cycle`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycle = mc(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 24), 3200.0)
        val past = CycleEngine.calculatePastCycles(listOf(cycle), emptyList(), today)
        assertTrue(past.isEmpty())
    }

    @Test
    fun `calculatePastCycles includes ended cycles without transactions`() {
        val today = LocalDate.of(2026, 6, 28)
        val cycle = mc(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 24), 3200.0)
        val past = CycleEngine.calculatePastCycles(listOf(cycle), emptyList(), today)
        assertEquals(1, past.size)
        assertEquals(3200.0, past[0].balance, 0.001)
    }

    @Test
    fun `windowForPayday uses previous payday when today is before this months payday`() {
        val today = LocalDate.of(2026, 6, 4)
        val (start, end) = CycleEngine.windowForPayday(25, today)
        assertEquals(LocalDate.of(2026, 5, 25), start)
        assertEquals(LocalDate.of(2026, 6, 24), end)
    }

    @Test
    fun `windowForPayday starts today when today is payday`() {
        val today = LocalDate.of(2026, 6, 25)
        val (start, end) = CycleEngine.windowForPayday(25, today)
        assertEquals(LocalDate.of(2026, 6, 25), start)
        assertEquals(LocalDate.of(2026, 7, 24), end)
    }

    @Test
    fun `windowForPayday clamps payday 31 in short months`() {
        val today = LocalDate.of(2026, 3, 1)
        val (start, end) = CycleEngine.windowForPayday(31, today)
        assertEquals(LocalDate.of(2026, 2, 28), start)
        assertEquals(LocalDate.of(2026, 3, 30), end)
    }

    @Test
    fun `resolveCurrentCycle prefers the window covering today`() {
        val today = LocalDate.of(2026, 6, 4)
        val previous = mc(LocalDate.of(2026, 4, 25), LocalDate.of(2026, 5, 24), 3000.0)
        val current = mc(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 24), 3200.0)
        val next = mc(LocalDate.of(2026, 6, 25), LocalDate.of(2026, 7, 24), 3200.0)
        val resolved = CycleEngine.resolveCurrentCycle(listOf(previous, current, next), today)
        assertEquals(current.startDate, resolved?.startDate)
    }

    @Test
    fun `buildExportCsv writes a header and a row per transaction`() {
        val cycle = mc(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 24), 3200.0)
        val txns = listOf(tx(LocalDate.of(2026, 6, 1), "lifestyle", "expense", 12.5))
        val csv = CycleEngine.buildExportCsv(listOf(cycle), txns)
        assertTrue(csv.startsWith("date,category,kind,amount,note,cycle"))
        assertTrue(csv.contains("lifestyle"))
        assertTrue(csv.contains("12.50"))
    }
}
