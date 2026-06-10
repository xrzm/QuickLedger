package com.quickledger.app.domain.repository

import com.quickledger.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getVisibleCategoriesByType(isIncome: Boolean): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    suspend fun updateSort(id: Long, sort: Int)
    suspend fun getDefaultCategories(isIncome: Boolean): List<Category>
    suspend fun initializeDefaultCategoriesIfNeeded()
}
