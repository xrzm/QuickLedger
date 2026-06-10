package com.quickledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.quickledger.app.domain.model.ThemeMode
import com.quickledger.app.domain.repository.AppSettingRepository
import com.quickledger.app.presentation.navigation.AppNavigation
import com.quickledger.app.presentation.theme.QuickLedgerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSettingRepository: AppSettingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            var isSettingsLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val settings = appSettingRepository.getSettingsOnce()
                settings?.let {
                    themeMode = it.themeMode
                }
                isSettingsLoaded = true
            }

            QuickLedgerTheme(themeMode = themeMode) {
                AppNavigation()
            }
        }
    }
}
