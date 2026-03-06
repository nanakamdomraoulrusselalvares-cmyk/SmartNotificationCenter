package com.smartnotification.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Create : Screen("create/{notificationId}") {
        fun createRoute(id: Int = -1) = "create/$id"
    }
    object Schedule : Screen("schedule")
    object History : Screen("history")
    object Priority : Screen("priority")
    object Settings : Screen("settings")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Filled.Home, Screen.Dashboard),
    BottomNavItem("Schedule", Icons.Filled.CalendarMonth, Screen.Schedule),
    BottomNavItem("History", Icons.Filled.History, Screen.History),
    BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings)
)
