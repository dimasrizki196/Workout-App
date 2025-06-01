package com.example.suwirgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.suwirgym.navigation.NavGraph
import com.example.suwirgym.ui.theme.SuwirGymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuwirGymTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
