package com.example.suwirgym.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.suwirgym.ui.components.AppBar
import com.example.suwirgym.ui.components.BottomBar
import com.example.suwirgym.ui.components.BottomNavItem
import com.example.suwirgym.ui.screens.graph.GraphScreen
import com.example.suwirgym.ui.screens.home.HomeScreen
import com.example.suwirgym.ui.screens.notification.NotificationScreen
import com.example.suwirgym.ui.screens.profile.ProfileScreen
import com.example.suwirgym.utils.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val items = listOf(
        BottomNavItem("Beranda", Icons.Default.Home, Screen.Home),
        BottomNavItem("Grafik", Icons.Default.Settings, Screen.Graph),
        BottomNavItem("Riwayat", Icons.Default.Notifications, Screen.Notification),
        BottomNavItem("Profil", Icons.Default.Person, Screen.Profile)
    )

    Scaffold(
        bottomBar = { BottomBar(navController = navController, items = items) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Graph.route) { GraphScreen() }
            composable(Screen.Notification.route) { NotificationScreen(userId = userId) }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}
