package com.knownassurajit.dvide_finance.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "cycles")
data class ManualCycle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: Int,
    val year: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val income: Double
)
