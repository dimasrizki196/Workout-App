package com.example.suwirgym.ui.screens.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.suwirgym.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
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
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var userId by remember { mutableStateOf<String?>(null) }
    var loadingUser by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repeat(5) {
            userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) return@repeat
            delay(500)
        }
        loadingUser = false
    }

    if (loadingUser) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (userId == null) {
        Text(
            text = "Anda belum login. Silakan login kembali.",
            color = Color.Red,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

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

    val days = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val monday = today.with(java.time.DayOfWeek.MONDAY)
    var workoutStatus by remember { mutableStateOf(List(7) { false }) }

    LaunchedEffect(userId) {
        val statuses = MutableList(7) { false }
        for (i in 0..6) {
            val dateToCheck = monday.plusDays(i.toLong())
            val doc = db.collection("users")
                .document(userId!!)
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

    fun markTodayWorkoutDone() {
        val todayIndex = (today.dayOfWeek.value + 6) % 7
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
                    Image(
                        painter = painterResource(
                            id = if (workoutStatus.getOrNull(index) == true)
                                R.drawable.fire_color
                            else
                                R.drawable.fire_gray
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = day, fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            onValueChange = {
                                repsMap = repsMap.toMutableMap().apply { put(workout.name, it) }
                            },
                            label = { Text("x") },
                            singleLine = true,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }
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
                val allFilled = repsMap.values.any { it.toIntOrNull() != null && it.toInt() > 0 }
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
                            markTodayWorkoutDone()
                            sendWorkoutNotification(context)
                            saveNotificationToFirestore(
                                userId!!,
                                title = "Workout Tersimpan",
                                message = "Workout kamu hari ini berhasil disimpan dan membakar ${"%.2f".format(totalCalories)} kcal!",
                                workouts = repsMap
                            )

                            scope.launch {
                                updateDayStreak(userId!!, dateStr)
                            }

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
    val userWorkoutRef = db.collection("users").document(userId)
        .collection("workouts").document(dateStr)

    userWorkoutRef.get().addOnSuccessListener { doc ->
        val oldReps = (doc.get("repsMap") as? Map<*, *>)
            ?.mapNotNull { (k, v) ->
                val key = k as? String
                val value = v as? String
                if (key != null && value != null) key to value else null
            }?.toMap() ?: emptyMap()
        val newReps = repsMap.toMutableMap()

        oldReps.forEach { (key, oldVal) ->
            val existing = newReps[key]?.toIntOrNull() ?: 0
            val previous = oldVal.toIntOrNull() ?: 0
            newReps[key] = (existing + previous).toString()
        }

        val oldCalories = doc.getDouble("totalCalories") ?: 0.0
        val updatedCalories = oldCalories + totalCalories

        val newData = hashMapOf(
            "repsMap" to newReps,
            "totalCalories" to updatedCalories,
            "timestamp" to System.currentTimeMillis()
        )

        userWorkoutRef.set(newData)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }

    }.addOnFailureListener {
        onComplete(false, it.localizedMessage)
    }
}

suspend fun updateDayStreak(userId: String, today: String) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)
    val streakField = "dayStreak"

    val userSnapshot = userRef.get().await()
    val lastWorkoutDate = userSnapshot.getString("lastWorkoutDate")

    val formatter = DateTimeFormatter.ISO_DATE
    val todayDate = LocalDate.parse(today, formatter)
    val yesterday = todayDate.minusDays(1)

    if (lastWorkoutDate == today) return

    val newStreak = if (lastWorkoutDate == yesterday.toString()) {
        (userSnapshot.getLong(streakField) ?: 0L) + 1
    } else {
        1
    }

    userRef.update(
        mapOf(
            streakField to newStreak,
            "lastWorkoutDate" to today
        )
    )
}


fun sendWorkoutNotification(context: Context) {
    val channelId = "workout_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Workout Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("🔥 Workout Disimpan!")
        .setContentText("Kerja bagus! Tetap semangat 💪")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(1, notification)
}

fun saveNotificationToFirestore(
    userId: String,
    title: String,
    message: String,
    workouts: Map<String, String>
) {
    val db = FirebaseFirestore.getInstance()
    val notifRef = db.collection("users").document(userId)
        .collection("notifications")
        .document()

    val data = mapOf(
        "title" to title,
        "message" to message,
        "timestamp" to System.currentTimeMillis(),
        "workouts" to workouts // Tambahkan data workout
    )

    notifRef.set(data)
}
