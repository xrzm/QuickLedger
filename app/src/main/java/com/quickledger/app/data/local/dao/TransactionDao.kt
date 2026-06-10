package com.quickledger.app.data.local.dao

import androidx.room.*
import com.quickledger.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY create_time DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY create_time DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE create_time BETWEEN :startTime AND :endTime
        ORDER BY create_time DESC
    """)
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE create_time BETWEEN :startTime AND :endTime
        ORDER BY create_time DESC
    """)
    suspend fun getTransactionsByDateRangeOnce(startTime: Long, endTime: Long): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions
        WHERE create_time BETWEEN :startTime AND :endTime AND type = :type
        ORDER BY create_time DESC
    """)
    fun getTransactionsByDateRangeAndType(
        startTime: Long, endTime: Long, type: String
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE (remark LIKE '%' || :query || '%' OR CAST(amount AS TEXT) LIKE '%' || :query || '%')
        ORDER BY create_time DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactions(ids: List<Long>)

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE type = :type AND create_time BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalByTypeAndDateRange(
        type: String, startTime: Long, endTime: Long
    ): Double

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM transactions t
        WHERE t.type = :type AND t.category_id = :categoryId
        AND t.create_time BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalByCategoryAndDateRange(
        type: String, categoryId: Long, startTime: Long, endTime: Long
    ): Double

    @Query("""
        SELECT t.category_id, COALESCE(SUM(t.amount), 0) as total
        FROM transactions t
        WHERE t.type = :type AND t.create_time BETWEEN :startTime AND :endTime
        GROUP BY t.category_id
        ORDER BY total DESC
    """)
    suspend fun getCategoryTotalsByDateRange(
        type: String, startTime: Long, endTime: Long
    ): List<CategoryTotal>

    data class CategoryTotal(
        @ColumnInfo(name = "category_id") val categoryId: Long,
        val total: Double
    )
}
