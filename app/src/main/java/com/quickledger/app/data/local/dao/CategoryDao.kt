package com.quickledger.app.data.local.dao

import androidx.room.*
import com.quickledger.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sort ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE is_income = :isIncome AND is_hidden = 0 ORDER BY sort ASC, id ASC")
    fun getVisibleCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE categories SET sort = :sort WHERE id = :id")
    suspend fun updateSort(id: Long, sort: Int)

    @Query("SELECT COUNT(*) FROM categories WHERE is_income = :isIncome")
    suspend fun getCategoryCount(isIncome: Boolean): Int
}
