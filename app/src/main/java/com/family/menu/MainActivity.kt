package com.family.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.family.menu.ui.components.BottomNavBar
import com.family.menu.ui.components.BottomTab
import com.family.menu.ui.navigation.Routes
import com.family.menu.ui.screen.CalendarDetailScreen
import com.family.menu.ui.screen.CalendarScreen
import com.family.menu.ui.screen.CategoryScreen
import com.family.menu.ui.screen.DishDetailScreen
import com.family.menu.ui.screen.DishEditScreen
import com.family.menu.ui.screen.HomeScreen
import com.family.menu.ui.screen.MineScreen
import com.family.menu.ui.screen.OrderScreen
import com.family.menu.ui.screen.SettingsScreen
import com.family.menu.ui.theme.FamilyMenuTheme

private val TAB_ROUTES = setOf(Routes.HOME, Routes.CALENDAR, Routes.MINE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FamilyMenuTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TAB_ROUTES.any { it == currentRoute } || currentRoute == null

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
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.CALENDAR) { CalendarScreen() }
            composable(Routes.MINE) { MineScreen() }
            composable(Routes.ORDER) { OrderScreen() }
            composable(Routes.CATEGORY) { CategoryScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.DISH_EDIT) {
                DishEditScreen()
            }
            composable(Routes.DISH_DETAIL) { backStackEntry ->
                DishDetailScreen()
            }
            composable(Routes.CALENDAR_DETAIL) {
                CalendarDetailScreen()
            }
        }
    }
}