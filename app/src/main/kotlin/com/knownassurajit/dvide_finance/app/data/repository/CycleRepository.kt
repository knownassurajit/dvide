package com.knownassurajit.dvide_finance.app.data.repository

import com.knownassurajit.dvide_finance.app.data.local.CycleDao
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao
) {
    fun observeAll(): Flow<List<ManualCycle>> = cycleDao.observeAll()

    suspend fun insert(cycle: ManualCycle): Long {
        return cycleDao.insert(cycle)
    }

    suspend fun update(cycle: ManualCycle) {
        cycleDao.update(cycle)
    }

    suspend fun delete(cycle: ManualCycle) {
        cycleDao.delete(cycle)
    }

    suspend fun getAll(): List<ManualCycle> {
        return cycleDao.getAll()
    }
}
