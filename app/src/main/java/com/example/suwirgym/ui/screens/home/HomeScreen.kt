package com.example.suwirgym.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WorkoutItem(
    val name: String,
    val emoji: String,
    val caloriesPerRep: Float
)

@Composable
fun HomeScreen() {
    val workouts = listOf(
        WorkoutItem("Push Up", "💪", 0.3f),
        WorkoutItem("Pull Up", "🧗", 1.0f),
        WorkoutItem("Sit Up", "🤸", 0.25f),
        WorkoutItem("Squat", "🏋️", 0.32f)
    )

    var repsMap by remember { mutableStateOf(workouts.associate { it.name to "" }.toMutableMap()) }

    val totalCalories = workouts.sumOf {
        val reps = repsMap[it.name]?.toIntOrNull() ?: 0
        (reps * it.caloriesPerRep).toDouble()
    }

    var apiCalled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Kirim otomatis jika semua latihan terisi
    LaunchedEffect(repsMap) {
        val allFilled = repsMap.values.all { it.toIntOrNull() != null && it.toInt() > 0 }
        if (allFilled && !apiCalled) {
            isLoading = true
            apiError = null
            scope.launch {
                submitWorkoutToFirestore(repsMap, totalCalories) { success, error ->
                    if (success) {
                        apiCalled = true
                    } else {
                        apiError = "Gagal kirim ke Firestore: $error"
                    }
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
            .padding(16.dp)
    ) {
        Text(
            text = "Workout Harian",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        DayStreak()

        Spacer(modifier = Modifier.height(16.dp))

        WorkoutList(workouts, repsMap) { name, newReps ->
            repsMap = repsMap.toMutableMap().apply { put(name, newReps) }
            apiCalled = false
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total Kalori Dibakar: ${"%.2f".format(totalCalories)} kcal",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        apiError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = Color.Red,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun submitWorkoutToFirestore(
    repsMap: Map<String, String>,
    totalCalories: Double,
    onComplete: (Boolean, String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val today = LocalDate.now()
    val dateStr = today.format(DateTimeFormatter.ISO_DATE) // e.g., 2025-06-02

    val data = hashMapOf(
        "repsMap" to repsMap,
        "totalCalories" to totalCalories,
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("workouts")
        .document(dateStr)
        .set(data)
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
}

@Composable
fun DayStreak() {
    val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    val todayIndex = remember {
        val today = LocalDate.now()
        when (today.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> 0
            java.time.DayOfWeek.TUESDAY -> 1
            java.time.DayOfWeek.WEDNESDAY -> 2
            java.time.DayOfWeek.THURSDAY -> 3
            java.time.DayOfWeek.FRIDAY -> 4
            java.time.DayOfWeek.SATURDAY -> 5
            java.time.DayOfWeek.SUNDAY -> 6
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF858383), RoundedCornerShape(24.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (index <= todayIndex) "🔥" else "⬜",
                    fontSize = 18.sp,
                    color = if (index <= todayIndex) Color(0xFFFF9800) else Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
                Text(text = day, fontSize = 10.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun WorkoutList(
    workouts: List<WorkoutItem>,
    repsMap: Map<String, String>,
    onRepsChange: (String, String) -> Unit
) {
    Column {
        workouts.forEach { workout ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = workout.emoji, fontSize = 30.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = workout.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${workout.caloriesPerRep} kcal per kali",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = repsMap[workout.name] ?: "",
                        onValueChange = { onRepsChange(workout.name, it) },
                        label = { Text("x") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}
