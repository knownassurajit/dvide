package com.dvide.app.util

import java.text.NumberFormat
import java.util.Locale

fun Double.formatMoney(decimals: Int = 0): String {
    val fmt = NumberFormat.getNumberInstance(Locale.UK).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return "£${fmt.format(this)}"
}

data class MoneyParts(val whole: String, val frac: String)

fun Double.moneyParts(): MoneyParts {
    val abs   = kotlin.math.abs(this)
    val whole = abs.toLong()
    val frac  = kotlin.math.round((abs - whole) * 100).toInt()
    val fmt   = NumberFormat.getNumberInstance(Locale.UK)
    return MoneyParts(fmt.format(whole), frac.toString().padStart(2, '0'))
}

fun Int.ordinal(): String = this.toString() + when {
    this in 11..13 -> "th"
    this % 10 == 1 -> "st"
    this % 10 == 2 -> "nd"
    this % 10 == 3 -> "rd"
    else           -> "th"
}
