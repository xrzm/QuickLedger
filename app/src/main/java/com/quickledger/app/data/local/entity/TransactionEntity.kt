package com.quickledger.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["create_time"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" or "expense"
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    val remark: String = "",
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis()
)
