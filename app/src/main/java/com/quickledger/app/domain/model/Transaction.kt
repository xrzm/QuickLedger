package com.quickledger.app.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "",
    val remark: String = "",
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
)

enum class TransactionType(val value: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun fromValue(value: String): TransactionType =
            entries.find { it.value == value } ?: EXPENSE
    }
}
