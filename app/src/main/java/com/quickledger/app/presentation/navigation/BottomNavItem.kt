package com.quickledger.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = "home", label = "首页",
        selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home
    )
    data object Bills : BottomNavItem(
        route = "bills", label = "账单",
        selectedIcon = Icons.AutoMirrored.Filled.ListAlt, unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt
    )
    data object Profile : BottomNavItem(
        route = "profile", label = "我的",
        selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items = listOf(Home, Bills, Profile)
    }
}
