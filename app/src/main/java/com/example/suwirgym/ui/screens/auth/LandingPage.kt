package com.example.suwirgym.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.suwirgym.utils.Screen

@Composable
fun LandingPage(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Placeholder for Logo
                Text(text = "SuwirGym Logo", modifier = Modifier.padding(bottom = 40.dp))

                Button(onClick = { navController.navigate(Screen.Register.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Register")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { navController.navigate(Screen.Login.route) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Login")
                }
            }
        }
    }
}
