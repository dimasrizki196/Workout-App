package com.example.suwirgym.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.suwirgym.ui.screens.auth.LoginScreen
import com.example.suwirgym.ui.screens.auth.RegisterScreen
import com.example.suwirgym.ui.screens.home.HomeScreen
import com.example.suwirgym.ui.screens.graph.GraphScreen
import com.example.suwirgym.ui.screens.notification.NotificationScreen
import com.example.suwirgym.ui.screens.profile.ProfileScreen
import com.example.suwirgym.ui.screens.MainScreen
import com.example.suwirgym.utils.Screen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Home.route) {
            MainScreen(navController)
        }
    }
}
