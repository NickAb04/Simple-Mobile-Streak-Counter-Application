package com.riystreak.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.riystreak.app.ui.about.AboutScreen
import com.riystreak.app.ui.addedit.AddEditStreakScreen
import com.riystreak.app.ui.addedit.AddEditStreakViewModel
import com.riystreak.app.ui.detail.StreakDetailScreen
import com.riystreak.app.ui.detail.StreakDetailViewModel
import com.riystreak.app.ui.home.HomeScreen
import com.riystreak.app.ui.home.HomeViewModel
import com.riystreak.app.ui.navigation.Screen
import com.riystreak.app.ui.onboarding.OnboardingScreen
import com.riystreak.app.ui.settings.SettingsScreen
import com.riystreak.app.ui.settings.SettingsViewModel
import com.riystreak.app.ui.theme.RiyStreakTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as RiyStreakApplication

        setContent {
            RiyStreakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RiyStreakAppNavigation(app = app)
                }
            }
        }
    }
}

@Composable
fun RiyStreakAppNavigation(app: RiyStreakApplication) {
    val navController = rememberNavController()
    val settings by app.appSettingsRepository.appSettingsFlow.collectAsState(initial = null)

    val startDestination = if (settings?.hasCompletedOnboarding == false) {
        Screen.Onboarding.route
    } else {
        Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.appSettingsRepository.setOnboardingCompleted(true)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(app.streakRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToAddStreak = { navController.navigate(Screen.AddEditStreak.createRoute()) },
                onNavigateToDetail = { streakId -> navController.navigate(Screen.StreakDetail.createRoute(streakId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(
            route = Screen.AddEditStreak.route,
            arguments = listOf(navArgument("streakId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val streakIdString = backStackEntry.arguments?.getString("streakId")
            val streakId = streakIdString?.toLongOrNull()
            val addEditViewModel: AddEditStreakViewModel = viewModel(
                factory = AddEditStreakViewModel.Factory(streakId, app.streakRepository)
            )
            AddEditStreakScreen(
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StreakDetail.route,
            arguments = listOf(navArgument("streakId") { type = NavType.LongType })
        ) { backStackEntry ->
            val streakId = backStackEntry.arguments?.getLong("streakId") ?: -1L
            val detailViewModel: StreakDetailViewModel = viewModel(
                factory = StreakDetailViewModel.Factory(streakId, app.streakRepository)
            )
            StreakDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.AddEditStreak.createRoute(id)) }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(app.appSettingsRepository)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
