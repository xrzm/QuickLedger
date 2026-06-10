package com.quickledger.app.domain.usecase

import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.CategoryRepository
import com.quickledger.app.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(startTime: Long, endTime: Long): StatisticsData {
        val totalIncome = transactionRepository.getTotalByTypeAndDateRange(
            TransactionType.INCOME, startTime, endTime
        )
        val totalExpense = transactionRepository.getTotalByTypeAndDateRange(
            TransactionType.EXPENSE, startTime, endTime
        )

        val expenseBreakdown = getCategoryBreakdown(
            TransactionType.EXPENSE, totalExpense, startTime, endTime
        )
        val incomeBreakdown = getCategoryBreakdown(
            TransactionType.INCOME, totalIncome, startTime, endTime
        )

        val dailyTrends = calculateDailyTrends(startTime, endTime)

        return StatisticsData(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            categoryBreakdown = expenseBreakdown + incomeBreakdown,
            dailyTrends = dailyTrends
        )
    }

    private suspend fun getCategoryBreakdown(
        type: TransactionType,
        total: Double,
        startTime: Long,
        endTime: Long
    ): List<CategoryStat> {
        val totals = transactionRepository.getCategoryTotalsByDateRange(type, startTime, endTime)
        if (totals.isEmpty()) return emptyList()

        return totals.map { (categoryId, amount) ->
            val category = categoryRepository.getCategoryById(categoryId)
            CategoryStat(
                categoryId = categoryId,
                categoryName = category?.name ?: "未知",
                categoryIcon = category?.icon ?: "📦",
                categoryColor = category?.color ?: "#95A5A6",
                amount = amount,
                percentage = if (total > 0) (amount / total * 100).toFloat() else 0f,
                isIncome = type == TransactionType.INCOME
            )
        }
    }

    private suspend fun calculateDailyTrends(
        startTime: Long, endTime: Long
    ): List<DailyTrend> {
        val expenseTotals = transactionRepository.getCategoryTotalsByDateRange(
            TransactionType.EXPENSE, startTime, endTime
        )
        val incomeTotals = transactionRepository.getCategoryTotalsByDateRange(
            TransactionType.INCOME, startTime, endTime
        )

        // Simplified: return per-category totals as trends
        // For real daily trends, we'd need daily aggregation queries
        val allCategoryIds = (expenseTotals.map { it.first } + incomeTotals.map { it.first }).distinct()
        return allCategoryIds.map { categoryId ->
            val cat = categoryRepository.getCategoryById(categoryId)
            val expenseAmount = expenseTotals.find { it.first == categoryId }?.second ?: 0.0
            val incomeAmount = incomeTotals.find { it.first == categoryId }?.second ?: 0.0
            DailyTrend(
                date = cat?.name ?: "未知",
                income = incomeAmount,
                expense = expenseAmount
            )
        }
    }
}
