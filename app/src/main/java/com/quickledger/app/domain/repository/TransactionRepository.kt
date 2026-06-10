package com.quickledger.app.domain.repository

import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>>
    suspend fun getTransactionsByDateRangeOnce(startTime: Long, endTime: Long): List<Transaction>
    fun getTransactionsByDateRangeAndType(
        startTime: Long, endTime: Long, type: TransactionType
    ): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteTransactions(ids: List<Long>)
    suspend fun getTotalByTypeAndDateRange(
        type: TransactionType, startTime: Long, endTime: Long
    ): Double
    suspend fun getCategoryTotalsByDateRange(
        type: TransactionType, startTime: Long, endTime: Long
    ): List<Pair<Long, Double>>
}
