package com.example.suwirgym.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.suwirgym.ui.screens.auth.LandingPage
import com.example.suwirgym.ui.screens.auth.LoginScreen
import com.example.suwirgym.ui.screens.auth.RegisterScreen
import com.example.suwirgym.ui.screens.MainScreen

@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        composable(Screen.Landing.route) {
            LandingPage(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Main.route) {
            MainScreen(navController)
        }
    }
}
