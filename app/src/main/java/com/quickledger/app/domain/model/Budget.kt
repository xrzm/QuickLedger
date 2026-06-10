package com.quickledger.app.domain.model

data class Budget(
    val id: Long = 0,
    val categoryId: Long = -1,
    val categoryName: String = "",
    val categoryIcon: String = "",
    val amount: Double,
    val spent: Double = 0.0,
    val percentage: Float = 0f
)
