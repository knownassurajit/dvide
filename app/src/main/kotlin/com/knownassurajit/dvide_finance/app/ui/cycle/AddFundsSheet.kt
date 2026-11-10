package com.knownassurajit.dvide_finance.app.ui.cycle

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.ui.entry.AddTransactionSheet
import com.knownassurajit.dvide_finance.app.ui.entry.MODE_INCOME
import java.time.LocalDate

@Composable
fun AddFundsSheet(
    onAddFunds: (amount: Double, note: String) -> Unit,
    modifier: Modifier = Modifier,
    editing: Transaction? = null,
    onUpdateFunds: ((Transaction) -> Unit)? = null,
    cycleStart: LocalDate? = null,
    cycleEnd: LocalDate? = null,
) {
    AddFundsSheet(
        onAddFunds = { amount, note, _ -> onAddFunds(amount, note) },
        modifier = modifier,
        editing = editing,
        onUpdateFunds = onUpdateFunds,
        cycleStart = cycleStart,
        cycleEnd = cycleEnd,
    )
}

@Composable
fun AddFundsSheet(
    onAddFunds: (amount: Double, note: String, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    editing: Transaction? = null,
    onUpdateFunds: ((Transaction) -> Unit)? = null,
    cycleStart: LocalDate? = null,
    cycleEnd: LocalDate? = null,
) {
    AddTransactionSheet(
        initialMode = MODE_INCOME,
        editing = editing,
        cycleStart = cycleStart,
        cycleEnd = cycleEnd,
        onUpdate = onUpdateFunds,
        onAdd = { newTx ->
            onAddFunds(newTx.amount, newTx.note, newTx.date)
        },
        modifier = modifier,
    )
}
