package com.quickledger.app.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.CategoryRepository
import com.quickledger.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillsUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val filterCategoryId: Long? = null,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val editingTransaction: Transaction? = null,
    val showEditDialog: Boolean = false
)

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    private var listJob: Job? = null

    init {
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactions(),
                categoryRepository.getAllCategories()
            ) { txs, cats ->
                txs.map { tx -> enrich(tx, cats) }
            }.collect { list ->
                _uiState.update { it.copy(transactions = list) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        listJob?.cancel()
        listJob = viewModelScope.launch {
            val cats = categoryRepository.getAllCategories().first()
            val flow = if (query.isNotBlank()) {
                transactionRepository.searchTransactions(query)
            } else {
                transactionRepository.getAllTransactions()
            }
            flow.collect { list ->
                _uiState.update { it.copy(transactions = list.map { tx -> enrich(tx, cats) }) }
            }
        }
    }

    fun setFilterType(type: TransactionType?) {
        _uiState.update { it.copy(filterType = type) }
        listJob?.cancel()
        listJob = viewModelScope.launch {
            val cats = categoryRepository.getAllCategories().first()
            val flow = if (type != null) {
                transactionRepository.getTransactionsByType(type)
            } else {
                transactionRepository.getAllTransactions()
            }
            flow.collect { list ->
                _uiState.update { it.copy(transactions = list.map { tx -> enrich(tx, cats) }) }
            }
        }
    }

    private fun enrich(tx: Transaction, cats: List<com.quickledger.app.domain.model.Category>): Transaction {
        val cat = cats.find { it.id == tx.categoryId }
        return tx.copy(
            categoryName = cat?.name ?: "未知",
            categoryIcon = cat?.icon ?: "📦",
            categoryColor = cat?.color ?: "#95A5A6"
        )
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val newSelected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
            state.copy(selectedIds = newSelected, isSelectionMode = newSelected.isNotEmpty())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            transactionRepository.deleteTransactions(_uiState.value.selectedIds.toList())
            _uiState.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { transactionRepository.deleteTransaction(transaction) }
    }

    fun showEditDialog(transaction: Transaction) {
        _uiState.update { it.copy(editingTransaction = transaction, showEditDialog = true) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingTransaction = null, showEditDialog = false) }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
            _uiState.update { it.copy(showEditDialog = false, editingTransaction = null) }
        }
    }
}
