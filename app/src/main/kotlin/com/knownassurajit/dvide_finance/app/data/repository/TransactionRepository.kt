package com.knownassurajit.dvide_finance.app.data.repository

import com.knownassurajit.dvide_finance.app.data.local.TransactionDao
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
) {
    fun observeAll(): Flow<List<Transaction>> = dao.observeAll()

    suspend fun insert(tx: Transaction): Long = dao.insert(tx)

    suspend fun insertAll(txns: List<Transaction>) = dao.insertAll(txns)

    suspend fun delete(tx: Transaction) = dao.delete(tx)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun getAll(): List<Transaction> = dao.getAll()
}
