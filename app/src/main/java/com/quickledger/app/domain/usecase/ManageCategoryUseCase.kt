package com.quickledger.app.domain.usecase

import com.quickledger.app.domain.model.Category
import com.quickledger.app.domain.repository.CategoryRepository
import javax.inject.Inject

class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun addCategory(name: String, icon: String, color: String, isIncome: Boolean): Long {
        require(name.isNotBlank()) { "分类名称不能为空" }
        return categoryRepository.insertCategory(
            Category(name = name, icon = icon, color = color, isIncome = isIncome)
        )
    }

    suspend fun updateCategory(category: Category) {
        require(category.name.isNotBlank()) { "分类名称不能为空" }
        categoryRepository.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryRepository.deleteCategory(category)
    }

    suspend fun updateSortOrder(categories: List<Category>) {
        categories.forEachIndexed { index, category ->
            categoryRepository.updateSort(category.id, index)
        }
    }
}
