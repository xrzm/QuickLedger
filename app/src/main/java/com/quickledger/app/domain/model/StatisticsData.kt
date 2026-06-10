package com.quickledger.app.domain.model

data class StatisticsData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryBreakdown: List<CategoryStat> = emptyList(),
    val dailyTrends: List<DailyTrend> = emptyList()
) {
    val expenseCategoryBreakdown: List<CategoryStat>
        get() = categoryBreakdown.filter { !it.isIncome }

    val incomeCategoryBreakdown: List<CategoryStat>
        get() = categoryBreakdown.filter { it.isIncome }
}

data class CategoryStat(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val amount: Double,
    val percentage: Float,
    val isIncome: Boolean = false
)

data class DailyTrend(
    val date: String, // yyyy-MM-dd
    val income: Double = 0.0,
    val expense: Double = 0.0
)
