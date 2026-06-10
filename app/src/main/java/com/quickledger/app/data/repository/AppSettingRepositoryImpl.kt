package com.quickledger.app.data.repository

import com.quickledger.app.data.local.dao.AppSettingDao
import com.quickledger.app.data.local.entity.AppSettingEntity
import com.quickledger.app.domain.model.AppSetting
import com.quickledger.app.domain.model.ThemeMode
import com.quickledger.app.domain.repository.AppSettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao
) : AppSettingRepository {

    override fun getSettings(): Flow<AppSetting?> =
        appSettingDao.getSettings().map { it?.toDomain() }

    override suspend fun getSettingsOnce(): AppSetting? =
        appSettingDao.getSettingsOnce()?.toDomain()

    override suspend fun updateSettings(settings: AppSetting) =
        appSettingDao.insertOrUpdateSettings(settings.toEntity())

    private fun AppSettingEntity.toDomain() = AppSetting(
        cycleStartDay = cycleStartDay,
        themeMode = ThemeMode.fromValue(themeMode)
    )

    private fun AppSetting.toEntity() = AppSettingEntity(
        id = 1,
        cycleStartDay = cycleStartDay,
        themeMode = themeMode.value
    )
}
