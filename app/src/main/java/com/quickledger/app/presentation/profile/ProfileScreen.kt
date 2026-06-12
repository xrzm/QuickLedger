package com.quickledger.app.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickledger.app.domain.model.Category
import com.quickledger.app.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("我的") })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cycle settings
            item {
                Text(
                    text = "记账周期",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "每月起始日",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..28).forEach { day ->
                                val isSelected = day == uiState.cycleStartDay
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setCycleStartDay(day) },
                                    label = { Text("$day") }
                                )
                            }
                        }
                    }
                }
            }

            // Expense categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "支出分类",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { viewModel.showAddCategoryDialog(false) }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新增")
                    }
                }
            }
            item {
                CategoryGrid(
                    categories = uiState.expenseCategories,
                    onEdit = viewModel::showEditCategoryDialog,
                    onDelete = viewModel::deleteCategory
                )
            }

            // Income categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "收入分类",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { viewModel.showAddCategoryDialog(true) }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新增")
                    }
                }
            }
            item {
                CategoryGrid(
                    categories = uiState.incomeCategories,
                    onEdit = viewModel::showEditCategoryDialog,
                    onDelete = viewModel::deleteCategory
                )
            }

            // Export / Import
            item {
                Text("数据管理", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp))
            }
            // Cycle picker for export
            item {
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = viewModel::previousExportCycle, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.ChevronLeft, "上", modifier = Modifier.size(20.dp))
                            }
                            Text(uiState.exportCycleLabel.ifEmpty { "选择周期" },
                                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = viewModel::nextExportCycle, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.ChevronRight, "下", modifier = Modifier.size(20.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Button(onClick = viewModel::exportSelectedPeriod, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp), enabled = !uiState.isExporting) {
                                Text("导出", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(onClick = viewModel::exportAll, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp), enabled = !uiState.isExporting) {
                                Text("全部", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(onClick = viewModel::importFromClipboard, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)) {
                                Text("导入", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // App info
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "极速记账 QuickLedger", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "版本 2.3.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "所有者: zm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3秒完成记账 · 桌面小组件 · MVVM架构",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Category edit dialog
    if (uiState.showCategoryDialog && uiState.editingCategory != null) {
        CategoryEditDialog(
            category = uiState.editingCategory!!,
            isNew = uiState.isNewCategory,
            onCategoryChange = viewModel::updateEditingCategory,
            onSave = viewModel::saveCategory,
            onDismiss = viewModel::hideCategoryDialog
        )
    }
}

@Composable
fun CategoryGrid(
    categories: List<Category>,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    if (categories.isEmpty()) {
        Text(
            text = "暂无分类",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
        )
        return
    }

    val columns = 4
    val rows = (categories.size + columns - 1) / columns
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < categories.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            CategoryCard(
                                category = categories[index],
                                onEdit = { onEdit(categories[index]) },
                                onDelete = { onDelete(categories[index]) }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val bgColor = try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor.copy(alpha = 0.15f),
        modifier = Modifier.clickable { showMenu = true }
    ) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = category.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = { showMenu = false; onEdit() }
                )
                DropdownMenuItem(
                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: Category,
    isNew: Boolean,
    onCategoryChange: (Category) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (isNew) "新增分类" else "编辑分类") },
        text = {
            Column {
                OutlinedTextField(
                    value = category.name,
                    onValueChange = { onCategoryChange(category.copy(name = it)) },
                    label = { Text("名称") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category.icon,
                    onValueChange = { onCategoryChange(category.copy(icon = it)) },
                    label = { Text("图标(emoji)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category.color,
                    onValueChange = { onCategoryChange(category.copy(color = it)) },
                    label = { Text("颜色(#RRGGBB)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = category.name.isNotBlank(),
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
