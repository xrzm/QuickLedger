package com.quickledger.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickledger.app.domain.model.*
import com.quickledger.app.domain.repository.*
import com.quickledger.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val cycleLabel: String = "",
    val cycleStartTime: Long = 0L,
    val cycleEndTime: Long = 0L,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val cycleExpenseTotals: List<Pair<Long, Double>> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val quickRecordAmount: String = "",
    val quickRecordRemark: String = "",
    val quickRecordCategory: Category? = null,
    val quickRecordType: TransactionType = TransactionType.EXPENSE,
    val showQuickRecord: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val appSettingRepository: AppSettingRepository,
    private val calculateCycleUseCase: CalculateCycleUseCase,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cycleStartDay = 9
    private var currentCycleStart = 0L
    private var currentCycleEnd = 0L

    private var cycleDataJob: Job? = null
    private var categoryJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategoriesIfNeeded()
            appSettingRepository.getSettings().collect { settings ->
                val day = settings?.cycleStartDay ?: 9
                cycleStartDay = day
                updateCycle()
            }
        }
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            launch {
                categoryRepository.getVisibleCategoriesByType(false).collect { cats ->
                    _uiState.update { it.copy(expenseCategories = cats) }
                }
            }
            launch {
                categoryRepository.getVisibleCategoriesByType(true).collect { cats ->
                    _uiState.update { it.copy(incomeCategories = cats) }
                }
            }
        }
    }

    private fun updateCycle() {
        val cycle = calculateCycleUseCase.getCurrentCycle(cycleStartDay)
        currentCycleStart = cycle.startTime
        currentCycleEnd = cycle.endTime
        _uiState.update { it.copy(cycleLabel = cycle.label, cycleStartTime = cycle.startTime, cycleEndTime = cycle.endTime) }
        loadCycleData()
    }

    private fun loadCycleData() {
        cycleDataJob?.cancel()
        cycleDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val income = transactionRepository.getTotalByTypeAndDateRange(TransactionType.INCOME, currentCycleStart, currentCycleEnd)
            val expense = transactionRepository.getTotalByTypeAndDateRange(TransactionType.EXPENSE, currentCycleStart, currentCycleEnd)
            _uiState.update { it.copy(totalIncome = income, totalExpense = expense, balance = income - expense) }

            // Load per-category expense totals for pie chart (full cycle, not just recent)
            val expenseTotals = transactionRepository.getCategoryTotalsByDateRange(
                TransactionType.EXPENSE, currentCycleStart, currentCycleEnd
            )
            _uiState.update { it.copy(cycleExpenseTotals = expenseTotals) }

            val cats = categoryRepository.getAllCategories().first()
            transactionRepository.getTransactionsByDateRange(currentCycleStart, currentCycleEnd).collect { transactions ->
                val enriched = transactions.take(10).map { tx ->
                    val cat = cats.find { c -> c.id == tx.categoryId }
                    tx.copy(categoryName = cat?.name ?: "未知", categoryIcon = cat?.icon ?: "📦", categoryColor = cat?.color ?: "#95A5A6")
                }
                _uiState.update { it.copy(recentTransactions = enriched) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun previousCycle() {
        val cycle = calculateCycleUseCase.getPreviousCycle(cycleStartDay, currentCycleStart)
        currentCycleStart = cycle.startTime; currentCycleEnd = cycle.endTime
        _uiState.update { it.copy(cycleLabel = cycle.label) }
        loadCycleData()
    }

    fun nextCycle() {
        val cycle = calculateCycleUseCase.getNextCycle(cycleStartDay, currentCycleEnd)
        currentCycleStart = cycle.startTime; currentCycleEnd = cycle.endTime
        _uiState.update { it.copy(cycleLabel = cycle.label) }
        loadCycleData()
    }

    fun showQuickRecord(category: Category, type: TransactionType) {
        _uiState.update { it.copy(showQuickRecord = true, quickRecordCategory = category, quickRecordType = type, quickRecordAmount = "", quickRecordRemark = "") }
    }

    fun hideQuickRecord() {
        _uiState.update { it.copy(showQuickRecord = false, quickRecordAmount = "", quickRecordRemark = "") }
    }

    fun onQuickRecordRemarkChange(remark: String) {
        _uiState.update { it.copy(quickRecordRemark = remark) }
    }

    fun onQuickRecordAmountChange(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(quickRecordAmount = amount) }
        }
    }

    fun submitQuickRecord() {
        val state = _uiState.value
        val amount = state.quickRecordAmount.toDoubleOrNull()
        val category = state.quickRecordCategory
        if (amount == null || amount <= 0 || category == null) return

        viewModelScope.launch {
            try {
                addTransactionUseCase(Transaction(amount = amount, type = state.quickRecordType, categoryId = category.id, remark = state.quickRecordRemark))
                _uiState.update { it.copy(showQuickRecord = false, quickRecordAmount = "", toastMessage = "已记录 ¥${String.format("%.2f", amount)}") }
                loadCycleData()
                delay(2000)
                _uiState.update { it.copy(toastMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "记录失败: ${e.message}") }
                delay(2000)
                _uiState.update { it.copy(toastMessage = null) }
            }
        }
    }

    fun clearToast() { _uiState.update { it.copy(toastMessage = null) } }
}
