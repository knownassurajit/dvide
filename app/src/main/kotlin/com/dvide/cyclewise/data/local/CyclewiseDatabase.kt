package com.dvide.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dvide.app.data.model.Transaction

@Database(
    entities  = [Transaction::class],
    version   = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CyclewiseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "cyclewise.db"
    }
}
