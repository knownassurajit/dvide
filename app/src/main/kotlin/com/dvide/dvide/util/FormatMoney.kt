package com.dvide.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private fun setMonetaryGroupingSeparator(symbols: DecimalFormatSymbols, separator: Char) {
    try {
        val method = symbols.javaClass.getMethod("setMonetaryGroupingSeparator", Char::class.java)
        method.invoke(symbols, separator)
    } catch (e: Exception) {
        // Fallback or ignore on older Android runtime versions
    }
}

fun Double.formatMoney(
    currencyCode: String = "GBP",
    regionCode: String = "GB",
    numberFormatOption: String = "DEFAULT",
    decimals: Int = 0
): String {
    val locale = try {
        Locale("", regionCode)
    } catch (e: Exception) {
        Locale.UK
    }
    
    val currency = try {
        Currency.getInstance(currencyCode)
    } catch (e: Exception) {
        Currency.getInstance("GBP")
    }

    val fmt = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currency
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }

    // Apply custom number format overrides if requested
    if (numberFormatOption != "DEFAULT" && fmt is DecimalFormat) {
        val symbols = fmt.decimalFormatSymbols
        when (numberFormatOption) {
            "COMMA_DECIMAL" -> {
                symbols.groupingSeparator = '.'
                symbols.decimalSeparator = ','
                symbols.monetaryDecimalSeparator = ','
                setMonetaryGroupingSeparator(symbols, '.')
            }
            "SPACE_DECIMAL" -> {
                symbols.groupingSeparator = ' '
                symbols.decimalSeparator = ','
                symbols.monetaryDecimalSeparator = ','
                setMonetaryGroupingSeparator(symbols, ' ')
            }
            "DOT_DECIMAL" -> {
                symbols.groupingSeparator = ','
                symbols.decimalSeparator = '.'
                symbols.monetaryDecimalSeparator = '.'
                setMonetaryGroupingSeparator(symbols, ',')
            }
        }
        fmt.decimalFormatSymbols = symbols
    }

    return fmt.format(this)
}

data class MoneyParts(
    val symbol: String,
    val whole: String,
    val frac: String,
    val decimalSeparator: String
)

fun Double.moneyParts(
    currencyCode: String = "GBP",
    regionCode: String = "GB",
    numberFormatOption: String = "DEFAULT"
): MoneyParts {
    val abs   = kotlin.math.abs(this)
    val whole = abs.toLong()
    val frac  = kotlin.math.round((abs - whole) * 100).toInt()

    val locale = try {
        Locale("", regionCode)
    } catch (e: Exception) {
        Locale.UK
    }

    val currency = try {
        Currency.getInstance(currencyCode)
    } catch (e: Exception) {
        Currency.getInstance("GBP")
    }

    val fmt = NumberFormat.getNumberInstance(locale)
    fmt.minimumFractionDigits = 0
    fmt.maximumFractionDigits = 0

    if (numberFormatOption != "DEFAULT" && fmt is DecimalFormat) {
        val symbols = fmt.decimalFormatSymbols
        when (numberFormatOption) {
            "COMMA_DECIMAL" -> {
                symbols.groupingSeparator = '.'
                symbols.decimalSeparator = ','
                symbols.monetaryDecimalSeparator = ','
                setMonetaryGroupingSeparator(symbols, '.')
            }
            "SPACE_DECIMAL" -> {
                symbols.groupingSeparator = ' '
                symbols.decimalSeparator = ','
                symbols.monetaryDecimalSeparator = ','
                setMonetaryGroupingSeparator(symbols, ' ')
            }
            "DOT_DECIMAL" -> {
                symbols.groupingSeparator = ','
                symbols.decimalSeparator = '.'
                symbols.monetaryDecimalSeparator = '.'
                setMonetaryGroupingSeparator(symbols, ',')
            }
        }
        fmt.decimalFormatSymbols = symbols
    }

    val symbol = try {
        currency.getSymbol(locale)
    } catch (e: Exception) {
        currency.symbol
    }

    val decimalSeparator = if (fmt is DecimalFormat) {
        fmt.decimalFormatSymbols.decimalSeparator.toString()
    } else {
        "."
    }

    return MoneyParts(
        symbol = symbol,
        whole = fmt.format(whole),
        frac = frac.toString().padStart(2, '0'),
        decimalSeparator = decimalSeparator
    )
}

fun Int.ordinal(): String = this.toString() + when {
    this in 11..13 -> "th"
    this % 10 == 1 -> "st"
    this % 10 == 2 -> "nd"
    this % 10 == 3 -> "rd"
    else           -> "th"
}
