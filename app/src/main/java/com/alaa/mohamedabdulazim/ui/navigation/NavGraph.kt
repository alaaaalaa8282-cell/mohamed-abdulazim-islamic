package com.alaa.mohamedabdulazim.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.alaa.mohamedabdulazim.ui.screens.*
import com.alaa.mohamedabdulazim.ui.theme.IslamicGold
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreen

sealed class Screen(val route: String, val titleAr: String, val icon: ImageVector) {
    object Home       : Screen("home",       "الرئيسية",  Icons.Filled.Home)
    object Prayer     : Screen("prayer",     "المواقيت",  Icons.Filled.AccessTime)
    object Adhkar     : Screen("adhkar",     "الأذكار",   Icons.Filled.MenuBook)
    object Qibla      : Screen("qibla",      "القبلة",    Icons.Filled.Explore)
    object Settings   : Screen("settings",   "الإعدادات", Icons.Filled.Settings)
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Prayer, Screen.Adhkar, Screen.Qibla, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = IslamicGreen,
                contentColor   = IslamicGold
            ) {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentDest = currentBackStack?.destination
                screens.forEach { screen ->
                    val selected = currentDest?.route == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon  = { Icon(screen.icon, contentDescription = screen.titleAr) },
                        label = { Text(screen.titleAr) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = IslamicGold,
                            selectedTextColor   = IslamicGold,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                            indicatorColor      = IslamicGreen.copy(alpha = 0.0f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route)     { HomeScreen() }
            composable(Screen.Prayer.route)   { PrayerTimesScreen() }
            composable(Screen.Adhkar.route)   { AdhkarScreen() }
            composable(Screen.Qibla.route)    { QiblaScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
