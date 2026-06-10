package com.quickledger.app.data.repository

import com.quickledger.app.data.local.dao.CategoryDao
import com.quickledger.app.data.local.entity.CategoryEntity
import com.quickledger.app.domain.model.Category
import com.quickledger.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override fun getVisibleCategoriesByType(isIncome: Boolean): Flow<List<Category>> =
        categoryDao.getVisibleCategoriesByType(isIncome).map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity())

    override suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category.toEntity())

    override suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category.toEntity())

    override suspend fun updateSort(id: Long, sort: Int) =
        categoryDao.updateSort(id, sort)

    override suspend fun getDefaultCategories(isIncome: Boolean): List<Category> {
        return if (isIncome) DEFAULT_INCOME_CATEGORIES else DEFAULT_EXPENSE_CATEGORIES
    }

    override suspend fun initializeDefaultCategoriesIfNeeded() {
        val expenseCount = categoryDao.getCategoryCount(false)
        if (expenseCount == 0) {
            DEFAULT_EXPENSE_CATEGORIES.forEachIndexed { index, category ->
                categoryDao.insertCategory(category.toEntity().copy(sort = index))
            }
        }
        val incomeCount = categoryDao.getCategoryCount(true)
        if (incomeCount == 0) {
            DEFAULT_INCOME_CATEGORIES.forEachIndexed { index, category ->
                categoryDao.insertCategory(category.toEntity().copy(sort = index))
            }
        }
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id, name = name, icon = icon, color = color,
        sort = sort, isIncome = isIncome, isHidden = isHidden
    )

    private fun Category.toEntity() = CategoryEntity(
        id = id, name = name, icon = icon, color = color,
        sort = sort, isIncome = isIncome, isHidden = isHidden
    )

    companion object {
        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            Category(name = "餐饮", icon = "🍔", color = "#FF6B6B", isIncome = false),
            Category(name = "购物", icon = "🛒", color = "#4ECDC4", isIncome = false),
            Category(name = "交通", icon = "🚗", color = "#45B7D1", isIncome = false),
            Category(name = "娱乐", icon = "🎮", color = "#96CEB4", isIncome = false),
            Category(name = "住房", icon = "🏠", color = "#FFEAA7", isIncome = false),
            Category(name = "医疗", icon = "💊", color = "#DDA0DD", isIncome = false),
            Category(name = "教育", icon = "📚", color = "#98D8C8", isIncome = false),
            Category(name = "数码", icon = "📱", color = "#F7DC6F", isIncome = false),
            Category(name = "旅行", icon = "✈️", color = "#BB8FCE", isIncome = false),
            Category(name = "其他", icon = "📦", color = "#95A5A6", isIncome = false)
        )

        val DEFAULT_INCOME_CATEGORIES = listOf(
            Category(name = "工资", icon = "💰", color = "#2ECC71", isIncome = true),
            Category(name = "奖金", icon = "🎁", color = "#27AE60", isIncome = true),
            Category(name = "兼职", icon = "💼", color = "#1ABC9C", isIncome = true),
            Category(name = "投资", icon = "📈", color = "#3498DB", isIncome = true),
            Category(name = "红包", icon = "🧧", color = "#E74C3C", isIncome = true),
            Category(name = "其他", icon = "📦", color = "#95A5A6", isIncome = true)
        )
    }
}
