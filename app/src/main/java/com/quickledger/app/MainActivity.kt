package com.quickledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.quickledger.app.domain.model.ThemeMode
import com.quickledger.app.presentation.navigation.AppNavigation
import com.quickledger.app.presentation.theme.QuickLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuickLedgerTheme(themeMode = ThemeMode.LIGHT) {
                AppNavigation()
            }
        }
    }
}
