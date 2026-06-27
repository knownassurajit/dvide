package com.knownassurajit.dvide_finance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle

@Database(
    entities  = [Transaction::class, ManualCycle::class],
    version   = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DvideDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun cycleDao(): CycleDao

    companion object {
        const val DATABASE_NAME = "dvide.db"
    }
}
