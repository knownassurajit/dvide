package com.knownassurajit.dvide_finance.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun observeAll(): Flow<List<ManualCycle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: ManualCycle): Long

    @Update
    suspend fun update(cycle: ManualCycle)

    @Delete
    suspend fun delete(cycle: ManualCycle)

    @Query("SELECT * FROM cycles")
    suspend fun getAll(): List<ManualCycle>
}
