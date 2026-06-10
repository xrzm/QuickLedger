package com.quickledger.app.domain.model

data class AppSetting(
    val cycleStartDay: Int = 9,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String): ThemeMode =
            entries.find { it.value == value } ?: SYSTEM
    }
}
