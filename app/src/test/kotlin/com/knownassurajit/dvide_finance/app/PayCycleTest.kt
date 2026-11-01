package com.knownassurajit.dvide_finance.app

import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.domain.model.Cycle
import com.knownassurajit.dvide_finance.app.domain.model.Metrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

/**
 * Pay Cycle conventions under test (see [Cycle]):
 * start/end inclusive · totalDays = span + 1 · remaining counts today and is >= 1 ·
 * next salary date = cycle end + 1 day. All math is calendar-date based (no clocks, zones or DST involved).
 */
class PayCycleTest {

    private fun cycle(start: LocalDate, end: LocalDate) =
        ManualCycle(id = 1, month = start.monthValue, year = start.year, startDate = start, endDate = end, income = 1000.0)

    private fun metrics(start: LocalDate, end: LocalDate, today: LocalDate): Metrics =
        CycleEngine.computeMetrics(cycle(start, end), emptyList(), today)

    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    // ───────────── total days & next salary ─────────────

    @Test
    fun `1 Aug to 30 Aug has 30 days and next salary on 31 Aug`() {
        val c = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 10)).cycle
        assertEquals(30, c.totalDays)
        assertEquals(d(2026, 8, 31), c.nextSalaryDate)
    }

    @Test
    fun `cycle ending on the 30th pays on the 31st and a 31 day month has 31 days`() {
        val c = metrics(d(2026, 7, 1), d(2026, 7, 31), d(2026, 7, 5)).cycle
        assertEquals(31, c.totalDays)
        assertEquals(d(2026, 8, 1), c.nextSalaryDate)
    }

    @Test
    fun `leap february has 29 days and rolls to 1 March`() {
        val c = metrics(d(2028, 2, 1), d(2028, 2, 29), d(2028, 2, 2)).cycle
        assertEquals(29, c.totalDays)
        assertEquals(d(2028, 3, 1), c.nextSalaryDate)
    }

    @Test
    fun `common year february has 28 days and rolls to 1 March`() {
        val c = metrics(d(2027, 2, 1), d(2027, 2, 28), d(2027, 2, 2)).cycle
        assertEquals(28, c.totalDays)
        assertEquals(d(2027, 3, 1), c.nextSalaryDate)
    }

    @Test
    fun `cycle crossing the year boundary counts days across december and january`() {
        val c = metrics(d(2026, 12, 25), d(2027, 1, 24), d(2026, 12, 31)).cycle
        assertEquals(31, c.totalDays)
        assertEquals(d(2027, 1, 25), c.nextSalaryDate)
    }

    @Test
    fun `cycle ending on 31 December pays on 1 January of the next year`() {
        assertEquals(d(2027, 1, 1), metrics(d(2026, 12, 1), d(2026, 12, 31), d(2026, 12, 2)).cycle.nextSalaryDate)
    }

    @Test
    fun `month in which clocks change still has its calendar day count`() {
        // North-American DST starts 8 Mar 2026; day counts must not depend on 23 h days.
        assertEquals(31, metrics(d(2026, 3, 1), d(2026, 3, 31), d(2026, 3, 8)).cycle.totalDays)
    }

    @Test
    fun `single day cycle has one day and pays the next day`() {
        val c = metrics(d(2026, 8, 5), d(2026, 8, 5), d(2026, 8, 5)).cycle
        assertEquals(1, c.totalDays)
        assertEquals(1, c.remaining)
        assertEquals(d(2026, 8, 6), c.nextSalaryDate)
    }

    @Test
    fun `next salary date always equals the payday window it was built from`() {
        // payday 25: window 25 Jul..24 Aug, salary lands on 25 Aug
        val (s1, e1) = CycleEngine.windowForPayday(25, d(2026, 8, 10))
        assertEquals(d(2026, 8, 25), metrics(s1, e1, d(2026, 8, 10)).cycle.nextSalaryDate)
        // payday 31 in a 28 day February: salary clamps to 28 Feb
        val (s2, e2) = CycleEngine.windowForPayday(31, d(2027, 2, 15))
        assertEquals(d(2027, 2, 28), metrics(s2, e2, d(2027, 2, 15)).cycle.nextSalaryDate)
    }

    // ───────────── remaining days (inclusive of today) ─────────────

    @Test
    fun `first day of the cycle has every day remaining and zero progress`() {
        val m = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 1))
        assertEquals(0, m.cycle.dayIndex)
        assertEquals(30, m.cycle.remaining)
        assertEquals(0f, m.cycle.progress, 0f)
        assertFalse(m.ended)
    }

    @Test
    fun `last day of the cycle has exactly one day remaining and is not ended`() {
        val m = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 30))
        assertEquals(29, m.cycle.dayIndex)
        assertEquals(1, m.cycle.remaining)
        assertFalse(m.ended)
    }

    @Test
    fun `the day after the cycle ends it is ended and remaining stays clamped at one`() {
        val m = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 31))
        assertTrue(m.ended)
        assertEquals(1, m.cycle.remaining) // never 0: it divides the daily allowance
    }

    @Test
    fun `before the cycle starts nothing has elapsed`() {
        val m = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 7, 20))
        assertEquals(0, m.cycle.dayIndex)
        assertEquals(30, m.cycle.remaining)
        assertFalse(m.ended)
    }

    @Test
    fun `remaining counts today across a month boundary`() {
        // 25 May..24 Jun is 31 days; on 31 May six days have passed, so 25 remain including today.
        val m = metrics(d(2026, 5, 25), d(2026, 6, 24), d(2026, 5, 31))
        assertEquals(31, m.cycle.totalDays)
        assertEquals(6, m.cycle.dayIndex)
        assertEquals(25, m.cycle.remaining)
    }

    @Test
    fun `remaining plus elapsed always equals total across every day of a cycle`() {
        val start = d(2028, 2, 10)
        val end = d(2028, 3, 9)
        var day = start
        while (!day.isAfter(end)) {
            val c = metrics(start, end, day).cycle
            assertEquals("on $day", c.totalDays, c.dayIndex + c.remaining)
            day = day.plusDays(1)
        }
    }

    @Test
    fun `invalid cycle with end before start does not crash and reports one day`() {
        val m = metrics(d(2026, 8, 30), d(2026, 8, 1), d(2026, 8, 10))
        assertEquals(1, m.cycle.totalDays)
        assertTrue(m.cycle.remaining >= 1)
    }

    // ───────────── labels ─────────────

    @Test
    fun `days text is singular only for one`() {
        assertEquals("1 day", Cycle.daysText(1))
        assertEquals("2 days", Cycle.daysText(2))
        assertEquals("30 days", Cycle.daysText(30))
    }

    @Test
    fun `days left text switches to cycle ended after the last day`() {
        assertEquals("1 day left", metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 30)).daysLeftText)
        assertEquals("30 days left", metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 1)).daysLeftText)
        assertEquals("Cycle ended", metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 31)).daysLeftText)
        assertEquals("Ended", metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 31)).daysLeftShort)
        assertEquals("5d left", metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 26)).daysLeftShort)
    }

    @Test
    fun `next salary label is a readable calendar date`() {
        val c = metrics(d(2026, 8, 1), d(2026, 8, 30), d(2026, 8, 10)).cycle
        assertEquals("Mon, 31 Aug 2026", c.nextSalaryLabel(Locale.UK))
    }
}
