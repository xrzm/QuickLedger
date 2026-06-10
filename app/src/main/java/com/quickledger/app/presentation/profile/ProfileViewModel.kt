package com.quickledger.app.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.*
import com.quickledger.app.domain.usecase.CalculateCycleUseCase
import com.quickledger.app.domain.usecase.ManageCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ProfileUiState(
    val cycleStartDay: Int = 9,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val editingCategory: Category? = null,
    val showCategoryDialog: Boolean = false,
    val isNewCategory: Boolean = true,
    val isIncomeCategory: Boolean = false,
    val isExporting: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appSettingRepository: AppSettingRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val calculateCycleUseCase: CalculateCycleUseCase,
    private val manageCategoryUseCase: ManageCategoryUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategoriesIfNeeded()
            appSettingRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(cycleStartDay = settings?.cycleStartDay ?: 9, themeMode = settings?.themeMode ?: ThemeMode.SYSTEM) }
            }
            categoryRepository.getVisibleCategoriesByType(false).collect { cats -> _uiState.update { it.copy(expenseCategories = cats) } }
            categoryRepository.getVisibleCategoriesByType(true).collect { cats -> _uiState.update { it.copy(incomeCategories = cats) } }
        }
    }

    fun setCycleStartDay(day: Int) {
        viewModelScope.launch {
            val clamped = day.coerceIn(1, 28)
            val current = appSettingRepository.getSettingsOnce()
            appSettingRepository.updateSettings((current ?: AppSetting()).copy(cycleStartDay = clamped))
            _uiState.update { it.copy(cycleStartDay = clamped) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val current = appSettingRepository.getSettingsOnce()
            appSettingRepository.updateSettings((current ?: AppSetting()).copy(themeMode = mode))
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun showAddCategoryDialog(isIncome: Boolean) {
        _uiState.update { it.copy(showCategoryDialog = true, isNewCategory = true, isIncomeCategory = isIncome, editingCategory = Category(name = "", icon = "📦", color = "#95A5A6", isIncome = isIncome)) }
    }
    fun showEditCategoryDialog(category: Category) { _uiState.update { it.copy(showCategoryDialog = true, isNewCategory = false, editingCategory = category) } }
    fun hideCategoryDialog() { _uiState.update { it.copy(showCategoryDialog = false, editingCategory = null) } }
    fun updateEditingCategory(category: Category) { _uiState.update { it.copy(editingCategory = category) } }

    fun saveCategory() {
        val cat = _uiState.value.editingCategory ?: return
        viewModelScope.launch {
            if (_uiState.value.isNewCategory) manageCategoryUseCase.addCategory(cat.name, cat.icon, cat.color, cat.isIncome)
            else manageCategoryUseCase.updateCategory(cat)
            _uiState.update { it.copy(showCategoryDialog = false, editingCategory = null) }
        }
    }

    fun deleteCategory(category: Category) { viewModelScope.launch { manageCategoryUseCase.deleteCategory(category) } }
    fun updateCategorySort(categories: List<Category>) { viewModelScope.launch { manageCategoryUseCase.updateSortOrder(categories) } }

    fun exportCurrentCycle() {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val s = appSettingRepository.getSettingsOnce()
                val day = s?.cycleStartDay ?: 9
                val cycle = calculateCycleUseCase.getCurrentCycle(day)
                val raw = transactionRepository.getTransactionsByDateRange(cycle.startTime, cycle.endTime).firstOrNull() ?: emptyList()

                if (raw.isEmpty()) {
                    Toast.makeText(appContext, "当前周期没有账单数据", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val csv = buildCsv(raw, cycle.label)

                val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("账单导出", csv))
                Toast.makeText(appContext, "已复制到剪贴板，可粘贴到任意应用", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(appContext, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun buildCsv(transactions: List<Transaction>, cycleLabel: String): String {
        val sb = StringBuilder()
        sb.appendLine("极速记账 - 账单导出")
        sb.appendLine("周期: $cycleLabel")
        sb.appendLine("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
        sb.appendLine()
        sb.appendLine("类型,分类,金额,备注,时间")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (tx in transactions) {
            val type = if (tx.type == TransactionType.INCOME) "收入" else "支出"
            sb.appendLine("$type,${tx.categoryName.ifEmpty { "未知" }},${tx.amount},\"${tx.remark.replace("\"", "\"\"")}\",${sdf.format(Date(tx.createTime))}")
        }
        return sb.toString()
    }
}
