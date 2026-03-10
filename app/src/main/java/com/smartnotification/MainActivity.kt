package com.smartnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.smartnotification.data.repository.NotificationRepository
import com.smartnotification.presentation.create.CreateScreen
import com.smartnotification.presentation.dashboard.DashboardScreen
import com.smartnotification.presentation.history.HistoryScreen
import com.smartnotification.presentation.navigation.Screen
import com.smartnotification.presentation.navigation.bottomNavItems
import com.smartnotification.presentation.priority.PriorityScreen
import com.smartnotification.presentation.schedule.ScheduleScreen
import com.smartnotification.presentation.settings.SettingsScreen
import com.smartnotification.presentation.settings.SettingsViewModel
import com.smartnotification.presentation.theme.SmartNotificationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: NotificationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // Init default priority settings on first launch
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    repository.initDefaultPrioritySettings()
                }
            }

            // Request POST_NOTIFICATIONS permission on Android 13+
            RequestNotificationPermission()

            SmartNotificationTheme(darkTheme = settingsState.darkMode) {
                MainNavHost()
            }
        }
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* permission result - handled gracefully */ }

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar = bottomRoutes.any { currentRoute?.startsWith(it.substringBefore("/")) == true || currentRoute == it }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onCreateClick = { navController.navigate(Screen.Create.createRoute()) },
                    onEditClick = { id -> navController.navigate(Screen.Create.createRoute(id)) }
                )
            }
            composable(
                route = Screen.Create.route,
                arguments = listOf(navArgument("notificationId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) {
                CreateScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    onEditClick = { id -> navController.navigate(Screen.Create.createRoute(id)) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onPrioritySettings = { navController.navigate(Screen.Priority.route) }
                )
            }
            composable(Screen.Priority.route) {
                PriorityScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
