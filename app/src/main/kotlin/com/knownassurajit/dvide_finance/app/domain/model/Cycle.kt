package com.knownassurajit.dvide_finance.app.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A pay cycle window with its derived day counts.
 *
 * Conventions (the engine and every screen share these):
 * - [start] and [end] are both **inclusive**, so [totalDays] = days between them + 1 (1 Aug..30 Aug = 30).
 * - [dayIndex] is 0 on the first day. [remaining] **counts today** (first day = [totalDays], last day = 1) and is
 *   clamped to at least 1, because it is the divisor of the daily allowance.
 * - Salary lands the day after the last day of the cycle: [nextSalaryDate] = [end] + 1 day.
 */
data class Cycle(
    val start: LocalDate,
    val end: LocalDate,
    val totalDays: Int,
    val dayIndex: Int,          // 0-based elapsed days
    val remaining: Int,         // days remaining incl. today (>= 1)
    val progress: Float,        // 0..1
) {
    /** The day immediately after this cycle ends, i.e. the next salary date. */
    val nextSalaryDate: LocalDate get() = end.plusDays(1)

    fun label(): String {
        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.UK)
        return "${start.format(fmt)} – ${end.format(fmt)}".uppercase()
    }

    /** e.g. "Mon, 31 Aug 2026". */
    fun nextSalaryLabel(locale: Locale = Locale.getDefault()): String =
        nextSalaryDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale))

    /** "30 days" / "1 day". */
    fun totalDaysLabel(): String = daysText(totalDays)

    companion object {
        fun daysText(n: Int): String = if (n == 1) "1 day" else "$n days"
    }
}
