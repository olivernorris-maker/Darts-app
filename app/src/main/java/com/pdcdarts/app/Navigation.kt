package com.pdcdarts.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pdcdarts.app.ui.screens.bracket.BracketScreen
import com.pdcdarts.app.ui.screens.calendar.CalendarScreen
import com.pdcdarts.app.ui.screens.rankings.RankingsScreen
import com.pdcdarts.app.ui.screens.results.ResultsScreen
import com.pdcdarts.app.ui.screens.tonight.TonightScreen
import com.pdcdarts.app.ui.screens.tournament.TournamentDetailScreen
import com.pdcdarts.app.ui.theme.*

private sealed class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Tonight : Tab("tonight", "Tonight", Icons.Default.Star)
    data object Calendar : Tab("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object Results : Tab("results", "Results", Icons.Default.EmojiEvents)
    data object Rankings : Tab("rankings", "Rankings", Icons.Default.Leaderboard)
}

private val TABS = listOf(Tab.Tonight, Tab.Calendar, Tab.Results, Tab.Rankings)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavBar(navController)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Tonight.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.Tonight.route) {
                TonightScreen()
            }
            composable(Tab.Calendar.route) {
                CalendarScreen(
                    onTournamentClick = { name ->
                        navController.navigate("tournament/${name.encode()}")
                    }
                )
            }
            composable(Tab.Results.route) {
                ResultsScreen()
            }
            composable(Tab.Rankings.route) {
                RankingsScreen()
            }
            composable(
                "tournament/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType }),
            ) { backStack ->
                val name = backStack.arguments?.getString("name")?.decode() ?: return@composable
                TournamentDetailScreen(
                    tournamentName = name,
                    onBack = navController::navigateUp,
                    onViewBracket = { t ->
                        navController.navigate("bracket/${t.encode()}")
                    },
                )
            }
            composable(
                "bracket/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType }),
            ) { backStack ->
                val name = backStack.arguments?.getString("name")?.decode() ?: return@composable
                BracketScreen(
                    tournamentName = name,
                    onBack = navController::navigateUp,
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = DarkSurface) {
        TABS.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(tab.icon, contentDescription = tab.label)
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGreen,
                    selectedTextColor = AccentGreen,
                    indicatorColor = AccentGreen.copy(alpha = 0.15f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}

// Simple URL-safe encoding for tournament names in navigation routes
private fun String.encode() = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.decode() = java.net.URLDecoder.decode(this, "UTF-8")
