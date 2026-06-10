package com.quickledger.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quickledger.app.data.local.dao.*
import com.quickledger.app.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun appSettingDao(): AppSettingDao
}
