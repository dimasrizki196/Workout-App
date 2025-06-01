package com.example.suwirgym.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suwirgym.R

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "HOME",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        )

        DayStreak()

        Spacer(modifier = Modifier.height(16.dp))

        WorkoutList()
    }
}

@Composable
fun DayStreak() {
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val activeDays = 4

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color(0xFFFFEBEE), RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(text = "🔥", fontSize = 18.sp, color = if (index < activeDays) Color(0xFFFF9800) else Color.LightGray)
                Text(text = day, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun WorkoutList() {
    val workouts = listOf("PUSH UP", "PULL UP", "SIT UP", "SQUAD")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        workouts.forEach {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏃‍♂️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = it, fontWeight = FontWeight.Bold)
                        Text(text = "10 menit", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
