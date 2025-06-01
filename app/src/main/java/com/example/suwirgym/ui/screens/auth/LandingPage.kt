// ====== LandingPage.kt ======
package com.example.suwirgym.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.suwirgym.utils.Screen

@Composable
fun LandingPage(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SUWIRGYM",
                color = Color.Red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 60.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = {
                    navController.navigate(Screen.Login.route)
                }, modifier = Modifier.weight(1f)) {
                    Text("LOGIN")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = {
                    navController.navigate(Screen.Register.route)
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("REGISTER", color = Color.White)
                }
            }
        }
    }
}