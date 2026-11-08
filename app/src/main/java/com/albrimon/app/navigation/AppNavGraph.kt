package com.albrimon.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.albrimon.app.presentation.auth.LoginScreen
import com.albrimon.app.presentation.auth.RegisterScreen
import com.albrimon.app.presentation.auth.WelcomeScreen
import com.albrimon.app.presentation.client.ClientDetailScreen
import com.albrimon.app.presentation.client.ClientListScreen
import com.albrimon.app.core.ui.components.ComingSoonScreen
import com.albrimon.app.presentation.home.HomeScreen
import com.albrimon.app.presentation.project.ProjectListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
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

        composable(Routes.HOME) { HomeScreen(navController = navController) }

        composable(Routes.CLIENTS) { ClientListScreen(navController = navController) }

        composable(
            route = Routes.CLIENT_DETAIL,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getInt("clientId") ?: 0
            ClientDetailScreen(clientId = clientId, navController = navController)
        }

        composable(Routes.PROJECTS) { ProjectListScreen(navController = navController) }

        // Proyectos de un solo cliente: el id llega como argumento y lo lee el ViewModel
        composable(
            route = Routes.CLIENT_PROJECTS,
            arguments = listOf(navArgument("clientId") { type = NavType.IntType })
        ) { ProjectListScreen(navController = navController) }

        composable(Routes.SCRAPS) {
            ComingSoonScreen(title = "Retazos", navController = navController)
        }

        composable(Routes.SOON) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Próximamente"
            ComingSoonScreen(title = title, navController = navController)
        }
    }
}
