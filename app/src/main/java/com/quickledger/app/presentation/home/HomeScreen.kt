package com.quickledger.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.presentation.theme.ExpenseColor
import com.quickledger.app.presentation.theme.IncomeColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cycle header
            item {
                CycleHeader(
                    cycleLabel = uiState.cycleLabel,
                    onPrevious = viewModel::previousCycle,
                    onNext = viewModel::nextCycle
                )
            }

            // Summary cards
            item {
                SummaryCards(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    balance = uiState.balance
                )
            }

            // Expense pie chart
            val expenseData = uiState.expenseCategories.map { cat ->
                val total = uiState.recentTransactions
                    .filter { it.categoryId == cat.id && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                Triple(cat.name, cat.icon, total)
            }.filter { it.third > 0 }.sortedByDescending { it.third }
            if (expenseData.isNotEmpty()) {
                item {
                    Text("支出占比", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    ExpensePieChart(expenseData, Modifier.fillMaxWidth().height(200.dp))
                }
            }

            // Quick record - expense categories
            item {
                Text(
                    text = "快捷记账 · 支出",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.expenseCategories) { category ->
                        CategoryChip(
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            onClick = {
                                viewModel.showQuickRecord(category, TransactionType.EXPENSE)
                            }
                        )
                    }
                }
            }

            // Quick record - income categories
            item {
                Text(
                    text = "快捷记账 · 收入",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.incomeCategories) { category ->
                        CategoryChip(
                            name = category.name,
                            icon = category.icon,
                            color = category.color,
                            onClick = {
                                viewModel.showQuickRecord(category, TransactionType.INCOME)
                            }
                        )
                    }
                }
            }

            // Recent transactions
            item {
                Text(
                    text = "最近账单",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无账单记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.recentTransactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }

        // Quick record dialog
        if (uiState.showQuickRecord) {
            QuickRecordDialog(
                category = uiState.quickRecordCategory,
                type = uiState.quickRecordType,
                amount = uiState.quickRecordAmount,
                remark = uiState.quickRecordRemark,
                onAmountChange = viewModel::onQuickRecordAmountChange,
                onRemarkChange = viewModel::onQuickRecordRemarkChange,
                onSubmit = viewModel::submitQuickRecord,
                onDismiss = viewModel::hideQuickRecord
            )
        }

        // Toast
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = uiState.toastMessage ?: "",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CycleHeader(cycleLabel: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上一周期")
        }
        Text(
            text = cycleLabel,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下一周期")
        }
    }
}

@Composable
fun SummaryCards(totalIncome: Double, totalExpense: Double, balance: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "收入",
            amount = totalIncome,
            color = IncomeColor,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "支出",
            amount = totalExpense,
            color = ExpenseColor,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "结余",
            amount = balance,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (amount < 0) "-¥${"%.0f".format(-amount)}" else "¥${"%.0f".format(amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryChip(name: String, icon: String, color: String, onClick: () -> Unit) {
    val bgColor = try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor.copy(alpha = 0.15f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: com.quickledger.app.domain.model.Transaction) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeColor else ExpenseColor
    val prefix = if (isIncome) "+" else "-"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = transaction.categoryIcon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.categoryName.ifEmpty { transaction.remark.ifEmpty { if (isIncome) "收入" else "支出" } },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${prefix}¥${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                Text(
                    text = dateFormat.format(Date(transaction.createTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickRecordDialog(
    category: com.quickledger.app.domain.model.Category?,
    type: TransactionType,
    amount: String,
    remark: String,
    onAmountChange: (String) -> Unit,
    onRemarkChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    if (category == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = category.icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${category.name} · ${if (type == TransactionType.INCOME) "收入" else "支出"}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "输入金额（回车自动保存）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    placeholder = { Text("0.00", textAlign = TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = onRemarkChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注（选填）") },
                    placeholder = { Text("例如：午餐、地铁、咖啡...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
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

@Composable
fun ExpensePieChart(data: List<Triple<String, String, Double>>, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFF96CEB4), Color(0xFFFFEAA7), Color(0xFFDDA0DD),
        Color(0xFF98D8C8), Color(0xFFF7DC6F), Color(0xFFBB8FCE), Color(0xFF95A5A6)
    )
    val total = data.sumOf { it.third }

    Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(150.dp)) {
            if (total <= 0) return@Canvas
            var startAngle = -90f
            val strokeW = 30f
            val r = (size.minDimension - strokeW) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            data.forEachIndexed { i, (_, _, amt) ->
                val sweep = (amt / total * 360).toFloat()
                drawArc(colors[i % colors.size], startAngle, sweep, false,
                    Offset(center.x - r, center.y - r), Size(r * 2, r * 2),
                    style = Stroke(strokeW))
                startAngle += sweep
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            data.forEachIndexed { i, (name, icon, amt) ->
                val pct = if (total > 0) (amt / total * 100).toInt() else 0
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(8.dp)) { drawCircle(colors[i % colors.size]) }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$icon $name", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    Text("${pct}%", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
