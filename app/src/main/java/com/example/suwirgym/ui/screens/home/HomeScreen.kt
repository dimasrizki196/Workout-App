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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WorkoutItem(
    val name: String,
    val emoji: String,
    val caloriesPerRep: Float
)

@Composable
fun HomeScreen() {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    val workouts = listOf(
        WorkoutItem("Push Up", "💪", 0.3f),
        WorkoutItem("Pull Up", "🧗", 1.0f),
        WorkoutItem("Sit Up", "🤸", 0.25f),
        WorkoutItem("Squat", "🏋", 0.32f)
    )

    var repsMap by remember { mutableStateOf(workouts.associate { it.name to "" }.toMutableMap()) }
    var apiCalled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_DATE
    val dateStr = today.format(formatter)

    // Hari Senin sampai Minggu
    val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val monday = today.with(java.time.DayOfWeek.MONDAY)

    // workoutStatus untuk DayStreak, true kalau hari itu sudah workout
    var workoutStatus by remember { mutableStateOf(List(7) { false }) }

    // Load status workout 7 hari ini dari Firestore (hanya sekali)
    LaunchedEffect(userId) {
        if (userId != null) {
            val statuses = MutableList(7) { false }
            for (i in 0..6) {
                val dateToCheck = monday.plusDays(i.toLong())
                val doc = db.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .document(dateToCheck.format(formatter))
                    .get()
                    .await()
                if (doc.exists()) {
                    statuses[i] = true
                }
            }
            workoutStatus = statuses
        }
    }

    // Fungsi update workoutStatus hari ini langsung saat simpan sukses
    fun markTodayWorkoutDone() {
        val todayIndex = (today.dayOfWeek.value + 6) % 7 // supaya Senin=0, Minggu=6
        workoutStatus = workoutStatus.toMutableList().also { it[todayIndex] = true }
    }

    val totalCalories = workouts.sumOf {
        val reps = repsMap[it.name]?.toIntOrNull() ?: 0
        reps * it.caloriesPerRep.toDouble()
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

        DayStreak(days = days, workoutStatus = workoutStatus)

        Spacer(modifier = Modifier.height(16.dp))

        WorkoutList(workouts, repsMap) { name, newReps ->
            repsMap = repsMap.toMutableMap().apply { put(name, newReps) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Total Kalori Dibakar: ${"%.2f".format(totalCalories)} kcal",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val allFilled = repsMap.values.all { it.toIntOrNull() != null && it.toInt() > 0 }
                if (!allFilled) {
                    apiError = "Lengkapi semua input repetisi dengan angka > 0"
                    return@Button
                }
                if (apiCalled) {
                    apiError = "Workout hari ini sudah disimpan"
                    return@Button
                }
                isLoading = true
                apiError = null
                scope.launch {
                    submitWorkoutToFirestore(userId, repsMap, totalCalories, dateStr) { success, error ->
                        if (success) {
                            apiCalled = true
                            markTodayWorkoutDone() // langsung update DayStreak UI
                        } else {
                            apiError = "Gagal kirim ke Firestore: $error"
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading && !apiCalled
        ) {
            Text(text = if (apiCalled) "Sudah Disimpan" else "Simpan Workout")
        }

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
    userId: String?,
    repsMap: Map<String, String>,
    totalCalories: Double,
    dateStr: String,
    onComplete: (Boolean, String?) -> Unit
) {
    if (userId == null) {
        onComplete(false, "User belum login")
        return
    }

    val db = FirebaseFirestore.getInstance()

    val data = hashMapOf(
        "repsMap" to repsMap,
        "totalCalories" to totalCalories,
        "timestamp" to System.currentTimeMillis()
    )

    val batch = db.batch()

    val globalRef = db.collection("workouts").document(dateStr)
    batch.set(globalRef, data)

    val userWorkoutRef = db.collection("users").document(userId)
        .collection("workouts").document(dateStr)
    batch.set(userWorkoutRef, data)

    batch.commit()
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
}

@Composable
fun DayStreak(days: List<String>, workoutStatus: List<Boolean>) {
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
                    text = if (workoutStatus.getOrNull(index) == true) "🔥" else "⬜",
                    fontSize = 18.sp,
                    color = if (workoutStatus.getOrNull(index) == true) Color(0xFFFF9800) else Color.LightGray,
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