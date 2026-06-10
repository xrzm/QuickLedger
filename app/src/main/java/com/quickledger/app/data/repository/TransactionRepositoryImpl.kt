package com.quickledger.app.data.repository

import com.quickledger.app.data.local.dao.TransactionDao
import com.quickledger.app.data.local.entity.TransactionEntity
import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type.value).map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startTime, endTime).map { list -> list.map { it.toDomain() } }

    override suspend fun getTransactionsByDateRangeOnce(startTime: Long, endTime: Long): List<Transaction> =
        transactionDao.getTransactionsByDateRangeOnce(startTime, endTime).map { it.toDomain() }

    override fun getTransactionsByDateRangeAndType(
        startTime: Long, endTime: Long, type: TransactionType
    ): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRangeAndType(startTime, endTime, type.value)
            .map { list -> list.map { it.toDomain() } }

    override fun searchTransactions(query: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactionDao.getTransactionById(id)?.toDomain()

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.updateTransaction(transaction.toEntity().copy(updateTime = System.currentTimeMillis()))

    override suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction.toEntity())

    override suspend fun deleteTransactions(ids: List<Long>) =
        transactionDao.deleteTransactions(ids)

    override suspend fun getTotalByTypeAndDateRange(
        type: TransactionType, startTime: Long, endTime: Long
    ): Double = transactionDao.getTotalByTypeAndDateRange(type.value, startTime, endTime)

    override suspend fun getCategoryTotalsByDateRange(
        type: TransactionType, startTime: Long, endTime: Long
    ): List<Pair<Long, Double>> =
        transactionDao.getCategoryTotalsByDateRange(type.value, startTime, endTime)
            .map { it.categoryId to it.total }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.fromValue(type),
        categoryId = categoryId,
        remark = remark,
        createTime = createTime,
        updateTime = updateTime
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        type = type.value,
        categoryId = categoryId,
        remark = remark,
        createTime = createTime,
        updateTime = updateTime
    )
}
