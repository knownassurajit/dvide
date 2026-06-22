package com.knownassurajit.dvide_finance.app.domain.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Cycle(
    val start: LocalDate,
    val end: LocalDate,
    val totalDays: Int,
    val dayIndex: Int,          // 0-based elapsed days
    val remaining: Int,         // days remaining (≥ 1)
    val progress: Float,        // 0..1
) {
    fun label(): String {
        val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.UK)
        return "${start.format(fmt)} – ${end.format(fmt)}".uppercase()
    }
}
