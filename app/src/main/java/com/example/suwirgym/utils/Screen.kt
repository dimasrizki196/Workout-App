package com.example.suwirgym.utils

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home")
    object Graph : Screen("graph")
    object Notification : Screen("notification")
    object Profile : Screen("profile")
}
