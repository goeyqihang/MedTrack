package com.qihang.medtrack.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomTab("home", "Home", Icons.Filled.Home)
    data object Symptoms : BottomTab("symptoms", "Symptoms", Icons.Filled.MonitorHeart)
    data object MedCoach : BottomTab("medcoach", "MedCoach", Icons.Filled.MedicalServices)
    data object Settings : BottomTab("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val all: List<BottomTab> by lazy { listOf(Home, Symptoms, MedCoach, Settings) }
        val routes: Set<String> by lazy { all.map { it.route }.toSet() }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        BottomTab.all.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}
