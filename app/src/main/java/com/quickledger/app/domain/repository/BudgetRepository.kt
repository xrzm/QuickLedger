package com.quickledger.app.domain.repository

import com.quickledger.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun getBudgetByCategory(categoryId: Long): Budget?
    suspend fun insertOrUpdateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
}
