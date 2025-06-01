package com.example.suwirgym.utils

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Home : Screen("home")
    object Graph : Screen("graph")
    object Notification : Screen("notification")
    object Profile : Screen("profile")
}


