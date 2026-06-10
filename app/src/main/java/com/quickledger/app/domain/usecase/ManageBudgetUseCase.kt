package com.quickledger.app.domain.usecase

import com.quickledger.app.domain.model.Budget
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.BudgetRepository
import com.quickledger.app.domain.repository.TransactionRepository
import com.quickledger.app.domain.usecase.CalculateCycleUseCase
import javax.inject.Inject

class ManageBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val calculateCycleUseCase: CalculateCycleUseCase
) {
    suspend fun getBudgetWithSpending(
        categoryId: Long, cycleStartDay: Int
    ): Budget? {
        val budget = budgetRepository.getBudgetByCategory(categoryId) ?: return null
        val cycle = calculateCycleUseCase.getCurrentCycle(cycleStartDay)
        val categorySpent = if (categoryId == -1L) {
            // Total budget: sum of all expenses in cycle
            transactionRepository.getTotalByTypeAndDateRange(
                TransactionType.EXPENSE, cycle.startTime, cycle.endTime
            )
        } else {
            // Category-specific budget: filter from category totals
            val totals = transactionRepository.getCategoryTotalsByDateRange(
                TransactionType.EXPENSE, cycle.startTime, cycle.endTime
            )
            totals.find { it.first == categoryId }?.second ?: 0.0
        }
        val percentage = if (budget.amount > 0) (categorySpent / budget.amount).toFloat() else 0f
        return budget.copy(spent = categorySpent, percentage = percentage)
    }

    suspend fun setBudget(categoryId: Long, amount: Double) {
        budgetRepository.insertOrUpdateBudget(
            Budget(categoryId = categoryId, amount = amount)
        )
    }
}
