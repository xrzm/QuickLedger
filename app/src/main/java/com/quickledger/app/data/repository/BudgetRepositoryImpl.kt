package com.quickledger.app.data.repository

import com.quickledger.app.data.local.dao.BudgetDao
import com.quickledger.app.data.local.entity.BudgetEntity
import com.quickledger.app.domain.model.Budget
import com.quickledger.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllBudgets().map { list -> list.map { it.toDomain() } }

    override suspend fun getBudgetByCategory(categoryId: Long): Budget? =
        budgetDao.getBudgetByCategory(categoryId)?.toDomain()

    override suspend fun insertOrUpdateBudget(budget: Budget) =
        budgetDao.insertOrUpdateBudget(budget.toEntity())

    override suspend fun deleteBudget(budget: Budget) =
        budgetDao.deleteBudget(budget.toEntity())

    private fun BudgetEntity.toDomain() = Budget(
        id = id, categoryId = categoryId, amount = amount
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id, categoryId = categoryId, amount = amount
    )
}
