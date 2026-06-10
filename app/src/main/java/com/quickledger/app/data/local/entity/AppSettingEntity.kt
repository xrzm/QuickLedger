package com.quickledger.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val id: Int = 1, // singleton row
    @ColumnInfo(name = "cycle_start_day")
    val cycleStartDay: Int = 9,
    @ColumnInfo(name = "theme_mode")
    val themeMode: String = "system" // "light", "dark", "system"
)
