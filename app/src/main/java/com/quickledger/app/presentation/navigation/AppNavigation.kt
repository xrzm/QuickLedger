package com.quickledger.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickledger.app.presentation.bills.BillsScreen
import com.quickledger.app.presentation.bills.BillsViewModel
import com.quickledger.app.presentation.home.HomeScreen
import com.quickledger.app.presentation.home.HomeViewModel
import com.quickledger.app.presentation.profile.ProfileScreen
import com.quickledger.app.presentation.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == item.route)
                                    item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(BottomNavItem.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                val vm: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = vm)
            }
            composable(BottomNavItem.Bills.route) {
                val vm: BillsViewModel = hiltViewModel()
                BillsScreen(viewModel = vm)
            }
            composable(BottomNavItem.Profile.route) {
                val vm: ProfileViewModel = hiltViewModel()
                ProfileScreen(viewModel = vm)
            }
        }
    }
}
