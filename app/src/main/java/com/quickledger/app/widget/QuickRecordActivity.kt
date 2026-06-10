package com.quickledger.app.widget

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickledger.app.domain.model.Transaction
import com.quickledger.app.domain.model.TransactionType
import com.quickledger.app.domain.repository.CategoryRepository
import com.quickledger.app.domain.repository.TransactionRepository
import com.quickledger.app.presentation.theme.QuickLedgerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class QuickRecordActivity : ComponentActivity() {

    @Inject lateinit var transactionRepo: TransactionRepository
    @Inject lateinit var categoryRepo: CategoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val name = intent?.getStringExtra("category") ?: "其他"
        val icon = intent?.getStringExtra("icon") ?: "📦"
        val isIncome = intent?.getBooleanExtra("isIncome", false) ?: false

        setContent {
            QuickLedgerTheme {
                var amount by remember { mutableStateOf("") }
                var saving by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                // Fully transparent background, card centered
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.88f),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(icon, fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$name · ${if (isIncome) "收入" else "支出"}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = amount,
                                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                placeholder = { Text("0.00", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { doSave(amount, name, isIncome, scope, { saving = it }) { finish() } }),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !saving
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { finish() }, modifier = Modifier.weight(1f)) { Text("取消") }
                                Button(
                                    onClick = { doSave(amount, name, isIncome, scope, { saving = it }) { finish() } },
                                    modifier = Modifier.weight(1f),
                                    enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && !saving
                                ) {
                                    if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    else Text("保存")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun doSave(
        amountStr: String, catName: String, isIncome: Boolean,
        scope: kotlinx.coroutines.CoroutineScope,
        onSaving: (Boolean) -> Unit, onDone: () -> Unit
    ) {
        val value = amountStr.toDoubleOrNull() ?: return
        if (value <= 0) return
        onSaving(true)
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    categoryRepo.initializeDefaultCategoriesIfNeeded()
                    val cats = categoryRepo.getAllCategories().first()
                    val cat = cats.find { it.name == catName } ?: cats.first()
                    transactionRepo.insertTransaction(Transaction(
                        amount = value,
                        type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                        categoryId = cat.id
                    ))
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuickRecordActivity, "已记录 ¥${"%.2f".format(value)}", Toast.LENGTH_SHORT).show()
                }
                onDone()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuickRecordActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                onSaving(false)
            }
        }
    }
}
