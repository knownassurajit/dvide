package com.knownassurajit.dvide_finance.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val category: String,
    val kind: String,           // "aside" | "expense"
    val amount: Double,
    val note: String,
)
