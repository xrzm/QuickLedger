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
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val editingCategory: Category? = null,
    val showCategoryDialog: Boolean = false,
    val isNewCategory: Boolean = true,
    val isIncomeCategory: Boolean = false,
    val isExporting: Boolean = false,
    val exportCycleLabel: String = "",
    val exportCycleStart: Long = 0L,
    val exportCycleEnd: Long = 0L
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

    init {
        loadData()
        viewModelScope.launch {
            val day = appSettingRepository.getSettingsOnce()?.cycleStartDay ?: 9
            cycleDay = day
            val cycle = calculateCycleUseCase.getCurrentCycle(day)
            _uiState.update { it.copy(exportCycleLabel = cycle.label, exportCycleStart = cycle.startTime, exportCycleEnd = cycle.endTime) }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategoriesIfNeeded()
            appSettingRepository.getSettings().collect { settings ->
                val day = settings?.cycleStartDay ?: 9
                cycleDay = day
                val cycle = calculateCycleUseCase.getCurrentCycle(day)
                _uiState.update { it.copy(cycleStartDay = day,
                    exportCycleLabel = cycle.label, exportCycleStart = cycle.startTime, exportCycleEnd = cycle.endTime) }
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

    private var cycleDay = 9

    fun previousExportCycle() {
        val s = _uiState.value
        val prev = calculateCycleUseCase.getPreviousCycle(cycleDay, s.exportCycleStart)
        _uiState.update { it.copy(exportCycleLabel = prev.label, exportCycleStart = prev.startTime, exportCycleEnd = prev.endTime) }
    }

    fun nextExportCycle() {
        val s = _uiState.value
        val next = calculateCycleUseCase.getNextCycle(cycleDay, s.exportCycleEnd)
        _uiState.update { it.copy(exportCycleLabel = next.label, exportCycleStart = next.startTime, exportCycleEnd = next.endTime) }
    }

    fun exportSelectedPeriod() {
        val s = _uiState.value
        doExport(s.exportCycleStart, s.exportCycleEnd, s.exportCycleLabel)
    }
    fun exportAll() { doExport(0L, Long.MAX_VALUE, "全部") }

    private fun doExport(start: Long, end: Long, label: String) {
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val raw = transactionRepository.getTransactionsByDateRange(start, end).firstOrNull() ?: emptyList()

                if (raw.isEmpty()) {
                    Toast.makeText(appContext, "没有账单数据", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cats = categoryRepository.getAllCategories().first()
                val enriched = raw.map { tx ->
                    val cat = cats.find { it.id == tx.categoryId }
                    tx.copy(categoryName = cat?.name ?: "未知", categoryIcon = cat?.icon ?: "📦")
                }
                val csv = buildCsv(enriched, label)
                val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("账单导出", csv))
                Toast.makeText(appContext, "已复制到剪贴板", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(appContext, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun importFromClipboard() {
        viewModelScope.launch {
            try {
                val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                if (text.isNullOrBlank()) {
                    Toast.makeText(appContext, "剪贴板为空，请先复制CSV内容", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val lines = text.lines().filter { it.isNotBlank() }
                // Skip header lines until we find the data header
                val headerIdx = lines.indexOfFirst { it.startsWith("类型,分类") }
                if (headerIdx == -1) {
                    Toast.makeText(appContext, "未识别到账单数据格式", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val cats = categoryRepository.getAllCategories().first()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                var count = 0
                for (i in (headerIdx + 1) until lines.size) {
                    val parts = lines[i].split(",")
                    if (parts.size < 3) continue
                    val type = if (parts[0].contains("收入")) TransactionType.INCOME else TransactionType.EXPENSE
                    val catName = parts[1].trim()
                    val amount = parts[2].trim().toDoubleOrNull() ?: continue
                    val remark = parts.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                    // Parse original date from CSV
                    var createTime = System.currentTimeMillis()
                    if (parts.size >= 5) {
                        try { sdf.parse(parts[4].trim())?.time?.let { createTime = it } } catch (_: Exception) {}
                    }
                    val cat = cats.find { it.name == catName }
                    if (cat == null) continue // skip if category name doesn't match exactly
                    if (amount > 0) {
                        transactionRepository.insertTransaction(
                            Transaction(amount = amount, type = type, categoryId = cat.id,
                                remark = remark, createTime = createTime)
                        )
                        count++
                    }
                }
                Toast.makeText(appContext, "导入完成，共 $count 条", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(appContext, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
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
