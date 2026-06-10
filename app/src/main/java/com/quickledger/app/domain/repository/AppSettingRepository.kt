package com.quickledger.app.domain.repository

import com.quickledger.app.domain.model.AppSetting
import kotlinx.coroutines.flow.Flow

interface AppSettingRepository {
    fun getSettings(): Flow<AppSetting?>
    suspend fun getSettingsOnce(): AppSetting?
    suspend fun updateSettings(settings: AppSetting)
}
