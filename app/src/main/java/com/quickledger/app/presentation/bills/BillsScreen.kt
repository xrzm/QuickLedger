package com.quickledger.app.presentation.bills

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.presentation.theme.ExpenseColor
import com.quickledger.app.presentation.theme.IncomeColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(viewModel: BillsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = if (uiState.isSelectionMode)
                        "已选 ${uiState.selectedIds.size} 项"
                    else "账单"
                )
            },
            navigationIcon = {
                if (uiState.isSelectionMode) {
                    IconButton(onClick = viewModel::clearSelection) {
                        Icon(Icons.Filled.Close, contentDescription = "取消选择")
                    }
                }
            },
            actions = {
                if (uiState.isSelectionMode) {
                    IconButton(onClick = viewModel::deleteSelected) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除选中")
                    }
                }
            }
        )

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("搜索金额、备注、分类...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "清除")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.filterType == null,
                onClick = { viewModel.setFilterType(null) },
                label = { Text("全部") }
            )
            FilterChip(
                selected = uiState.filterType == TransactionType.EXPENSE,
                onClick = { viewModel.setFilterType(TransactionType.EXPENSE) },
                label = { Text("支出") }
            )
            FilterChip(
                selected = uiState.filterType == TransactionType.INCOME,
                onClick = { viewModel.setFilterType(TransactionType.INCOME) },
                label = { Text("收入") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction list
        if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无账单",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    BillTransactionItem(
                        transaction = transaction,
                        isSelected = transaction.id in uiState.selectedIds,
                        isSelectionMode = uiState.isSelectionMode,
                        onToggleSelection = { viewModel.toggleSelection(transaction.id) },
                        onEdit = { viewModel.showEditDialog(transaction) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    // Edit dialog
    if (uiState.showEditDialog && uiState.editingTransaction != null) {
        EditTransactionDialog(
            transaction = uiState.editingTransaction!!,
            onSave = viewModel::updateTransaction,
            onDismiss = viewModel::hideEditDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillTransactionItem(
    transaction: com.quickledger.app.domain.model.Transaction,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeColor else ExpenseColor
    val prefix = if (isIncome) "+" else "-"

    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.clickable {
            if (isSelectionMode) onToggleSelection()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelectionMode) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(text = transaction.categoryIcon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.categoryName.ifEmpty { if (isIncome) "收入" else "支出" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dateFormat.format(Date(transaction.createTime)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.remark.isNotEmpty()) {
                        Text(
                            text = transaction.remark,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${prefix}¥${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                if (!isSelectionMode) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("编辑") },
                                onClick = { showMenu = false; onEdit() },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: com.quickledger.app.domain.model.Transaction,
    onSave: (com.quickledger.app.domain.model.Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var remark by remember { mutableStateOf(transaction.remark) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("编辑账单") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newAmount = amount.toDoubleOrNull()
                    if (newAmount != null && newAmount > 0) {
                        onSave(transaction.copy(amount = newAmount, remark = remark))
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
