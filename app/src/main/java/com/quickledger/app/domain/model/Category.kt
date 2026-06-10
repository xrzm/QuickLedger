package com.quickledger.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val sort: Int = 0,
    val isIncome: Boolean = false,
    val isHidden: Boolean = false
)
