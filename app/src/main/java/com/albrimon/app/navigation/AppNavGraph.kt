package com.albrimon.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albrimon.app.presentation.auth.LoginScreen
import com.albrimon.app.presentation.auth.RegisterScreen
import com.albrimon.app.presentation.auth.WelcomeScreen
import com.albrimon.app.presentation.client.ClientDetailScreen
import com.albrimon.app.presentation.client.ClientFormScreen
import com.albrimon.app.presentation.client.ClientListScreen
import com.albrimon.app.core.ui.components.AlbrimonLoadingScreen
import com.albrimon.app.core.ui.components.ComingSoonScreen
import com.albrimon.app.presentation.cost.CostCalculatorScreen
import com.albrimon.app.presentation.home.HomeScreen
import com.albrimon.app.presentation.project.ProjectListScreen
import com.albrimon.app.presentation.scrap.CutPlanScreen
import com.albrimon.app.presentation.scrap.ScrapFormScreen
import com.albrimon.app.presentation.scrap.ScrapListScreen

@Composable
fun AppNavGraph(startViewModel: StartViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // Si ya hay sesión guardada (DataStore) se entra directo a Inicio al reabrir la app.
    val startDestination by startViewModel.startDestination.collectAsStateWithLifecycle()
    val start = startDestination
    if (start == null) {
        AlbrimonLoadingScreen()
        return
    }

    NavHost(
        navController = navController,
        startDestination = start
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStart = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                onLogout = {
                    // Limpia toda la pila para que "atrás" no regrese a pantallas con sesión
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CLIENTS) { ClientListScreen(navController = navController) }

        composable(
            route = Routes.CLIENT_DETAIL,
            arguments = listOf(navArgument("clientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getLong("clientId") ?: 0L
            ClientDetailScreen(clientId = clientId, navController = navController)
        }

        composable(Routes.CLIENT_FORM) { ClientFormScreen(navController = navController) }

        composable(Routes.PROJECTS) { ProjectListScreen(navController = navController) }

        // Proyectos de un solo cliente: el id llega como argumento y lo lee el ViewModel
        composable(
            route = Routes.CLIENT_PROJECTS,
            arguments = listOf(navArgument("clientId") { type = NavType.LongType })
        ) { ProjectListScreen(navController = navController) }

        composable(Routes.SCRAPS) { ScrapListScreen(navController = navController) }

        composable(Routes.COSTS) { CostCalculatorScreen(navController = navController) }

        // scrapId = 0 significa "registrar retazo nuevo"; ScrapFormViewModel lo lee con SavedStateHandle
        composable(
            route = Routes.SCRAP_FORM,
            arguments = listOf(navArgument("scrapId") { type = NavType.LongType })
        ) { ScrapFormScreen(navController = navController) }

        composable(Routes.SCRAP_PLAN) { CutPlanScreen(navController = navController) }

        composable(Routes.SOON) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Próximamente"
            ComingSoonScreen(title = title, navController = navController)
        }
    }
}
