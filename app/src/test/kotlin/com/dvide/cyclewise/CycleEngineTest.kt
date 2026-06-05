package com.dvide.app

import com.dvide.app.data.model.Transaction
import com.dvide.app.domain.engine.CycleEngine
import com.dvide.app.domain.model.Cycle
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleEngineTest {

    // ─────────────────────────────────────────────────────────────
    // cycleFor — window derivation
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `cycleFor returns current cycle when today is after anchor`() {
        // Anchor = 25, today = June 4 → cycle is May 25 – June 24
        val today = LocalDate.of(2026, 6, 4)
        val cycle = CycleEngine.cycleFor(today, anchorDay = 25)

        assertEquals(LocalDate.of(2026, 5, 25), cycle.start)
        assertEquals(LocalDate.of(2026, 6, 24), cycle.end)
        assertEquals(31, cycle.totalDays)
        assertEquals(10, cycle.dayIndex)   // 10 days elapsed (inclusive day 0 = May 25)
        assertEquals(21, cycle.remaining)
    }

    @Test
    fun `cycleFor returns previous month cycle when today is before anchor`() {
        // Anchor = 25, today = June 20 → today >= anchor(25)? No, 20 < 25 → go back
        val today = LocalDate.of(2026, 6, 20)
        val cycle = CycleEngine.cycleFor(today, anchorDay = 25)

        assertEquals(LocalDate.of(2026, 5, 25), cycle.start)
        assertEquals(LocalDate.of(2026, 6, 24), cycle.end)
        assertEquals(10 + (20 - 4), cycle.dayIndex) // dayIndex = 26
    }

    @Test
    fun `cycleFor on anchor day starts new cycle`() {
        val today = LocalDate.of(2026, 6, 25)
        val cycle = CycleEngine.cycleFor(today, anchorDay = 25)

        assertEquals(LocalDate.of(2026, 6, 25), cycle.start)
        assertEquals(LocalDate.of(2026, 7, 24), cycle.end)
        assertEquals(0, cycle.dayIndex)
    }

    @Test
    fun `cycleFor clamps anchor day for short months`() {
        // Anchor = 31, February only has 28 days
        val today = LocalDate.of(2026, 2, 15)
        val cycle = CycleEngine.cycleFor(today, anchorDay = 31)

        // Feb has 28 days, so anchor clamps to 28.  Cycle: Jan 31 – Feb 27
        assertEquals(LocalDate.of(2026, 1, 31), cycle.start)
        assertEquals(LocalDate.of(2026, 2, 27), cycle.end)
    }

    @Test
    fun `progress is between 0 and 1`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycle = CycleEngine.cycleFor(today, 25)
        assertTrue(cycle.progress in 0f..1f)
    }

    @Test
    fun `remaining is never less than 1`() {
        // Last day of cycle
        val today = LocalDate.of(2026, 6, 24)
        val cycle = CycleEngine.cycleFor(today, 25)
        assertTrue(cycle.remaining >= 1)
    }

    // ─────────────────────────────────────────────────────────────
    // computeMetrics — financial waterfall
    // ─────────────────────────────────────────────────────────────

    private fun tx(date: LocalDate, cat: String, kind: String, amount: Double) = Transaction(
        id = 0, date = date, category = cat, kind = kind, amount = amount, note = ""
    )

    @Test
    fun `computeMetrics healthy scenario produces positive balance`() {
        val today = LocalDate.of(2026, 6, 4)
        val cycleStart = LocalDate.of(2026, 5, 25)

        val txns = listOf(
            tx(cycleStart,               "savings",    "aside",   400.0),
            tx(cycleStart,               "investment", "aside",   300.0),
            tx(cycleStart.plusDays(1),   "security",   "aside",   150.0),
            tx(cycleStart.plusDays(1),   "essentials", "expense",  48.20),
            tx(cycleStart.plusDays(3),   "lifestyle",  "expense",  34.00),
            tx(cycleStart.plusDays(7),   "essentials", "expense",  18.40),
            tx(cycleStart.plusDays(10),  "lifestyle",  "expense",   4.80),
        )

        val m = CycleEngine.computeMetrics(3200.0, 25, txns, today)

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

        val m = CycleEngine.computeMetrics(3200.0, 25, txns, today)

        assertTrue("Expected tight=true for over-spent scenario", m.tight)
        assertTrue(m.balance < m.spendable)
    }

    @Test
    fun `computeMetrics overspent scenario produces negative balance`() {
        val today = LocalDate.of(2026, 6, 15)
        val cycleStart = LocalDate.of(2026, 5, 25)

        val txns = listOf(
            tx(cycleStart,               "essentials", "expense", 2800.0),
            tx(cycleStart.plusDays(5),   "lifestyle",  "expense",  600.0),
            tx(cycleStart.plusDays(10),  "lifestyle",  "expense",  400.0),
        )

        val m = CycleEngine.computeMetrics(3200.0, 25, txns, today)

        assertTrue(m.balance < 0)
        assertTrue(m.borrowed > 0)
        assertEquals(0.0, m.surplus, 0.001)
    }

    @Test
    fun `computeMetrics ignores transactions outside the cycle`() {
        val today      = LocalDate.of(2026, 6, 4)
        val beforeCycle = LocalDate.of(2026, 4, 10)  // outside window

        val txns = listOf(
            tx(beforeCycle, "essentials", "expense", 500.0),
        )

        val m = CycleEngine.computeMetrics(3200.0, 25, txns, today)

        assertEquals(0.0, m.spent, 0.001)
        assertEquals(3200.0, m.balance, 0.001)
    }

    @Test
    fun `safeToSpend is balance divided by remaining days`() {
        val today = LocalDate.of(2026, 6, 4)
        val m     = CycleEngine.computeMetrics(3200.0, 25, emptyList(), today)

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
}
