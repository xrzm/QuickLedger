package com.quickledger.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val sort: Int = 0,
    @ColumnInfo(name = "is_income")
    val isIncome: Boolean = false,
    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false
)
