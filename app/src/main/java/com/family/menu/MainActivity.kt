package com.family.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.family.menu.data.repository.ThemeMode
import com.family.menu.ui.components.BottomNavBar
import com.family.menu.ui.components.DefaultBottomTabs
import com.family.menu.ui.navigation.Routes
import com.family.menu.ui.screen.CalendarDetailScreen
import com.family.menu.ui.screen.CalendarScreen
import com.family.menu.ui.screen.CategoryScreen
import com.family.menu.ui.screen.DishDetailScreen
import com.family.menu.ui.screen.DishEditScreen
import com.family.menu.ui.screen.DishManageScreen
import com.family.menu.ui.screen.HomeScreen
import com.family.menu.ui.screen.MineScreen
import com.family.menu.ui.screen.OrderScreen
import com.family.menu.ui.screen.PosterScreen
import com.family.menu.ui.screen.SettingsScreen
import com.family.menu.ui.screen.StatsScreen
import com.family.menu.ui.theme.FamilyMenuTheme

private val TAB_ROUTES = setOf(Routes.HOME, Routes.CALENDAR, Routes.MINE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 外观：0 跟随系统 / 1 浅色 / 2 深色
            val app = LocalContext.current.applicationContext as FamilyMenuApp
            val themeMode by app.container.settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }
            FamilyMenuTheme(darkTheme = darkTheme) {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TAB_ROUTES.contains(currentRoute)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute = currentRoute) { tab ->
                    if (tab.route != currentRoute) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenDish = { id -> navController.navigate(Routes.dishDetail(id)) },
                    onOpenOrder = { navController.navigate(Routes.ORDER) }
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(onOpenDate = { date -> navController.navigate(Routes.calendarDetail(date)) })
            }
            composable(Routes.MINE) { MineScreen(navController) }
            composable(Routes.ORDER) {
                OrderScreen(
                    onBack = { navController.popBackStack() },
                    onGoHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onGoPoster = { date -> navController.navigate(Routes.poster(date)) }
                )
            }

            composable(Routes.DISH_EDIT) {
                DishEditScreen(navController = navController)
            }
            composable(Routes.DISH_DETAIL) { entry ->
                val dishId = entry.arguments?.getString("dishId")?.toLongOrNull() ?: 0L
                if (dishId > 0L) {
                    DishDetailScreen(
                        dishId = dishId,
                        onEdit = { id -> navController.navigate(Routes.dishEdit(id)) },
                        onBack = { navController.popBackStack() },
                        onAdded = {
                            android.widget.Toast.makeText(
                                context, "已加入今日点单", android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            composable(Routes.CATEGORY) {
                CategoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.DISH_MANAGE) {
                DishManageScreen(navController = navController)
            }
            composable(Routes.STATS) {
                StatsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.CALENDAR_DETAIL) { entry ->
                val date = entry.arguments?.getString("date") ?: return@composable
                CalendarDetailScreen(
                    date = date,
                    onBack = { navController.popBackStack() },
                    onOpenDish = { id -> navController.navigate(Routes.dishDetail(id)) },
                    onGoOrder = { navController.navigate(Routes.ORDER) }
                )
            }
            composable(Routes.POSTER) { entry ->
                val date = entry.arguments?.getString("date") ?: return@composable
                PosterScreen(date = date, onBack = { navController.popBackStack() })
            }
        }
    }
}