package com.example.suwirgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.suwirgym.navigation.RootNavGraph
import com.example.suwirgym.ui.theme.SuwirGymTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            SuwirGymTheme {
                val navController = rememberNavController()
                RootNavGraph(navController = navController)
            }
        }
    }
}
