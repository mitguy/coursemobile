package edu.corp.glitch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.corp.glitch.ui.screens.auth.LoginScreen
import edu.corp.glitch.ui.screens.auth.RegisterScreen

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = onLoginSuccess,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

