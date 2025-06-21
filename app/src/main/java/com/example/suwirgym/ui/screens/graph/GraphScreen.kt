package com.example.suwirgym.ui.screens.graph

import androidx.compose.foundation.background
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GraphScreen(userId: String?) {
    val db = FirebaseFirestore.getInstance()
    var workoutData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var useBarChart by remember { mutableStateOf(false) }
    var dayStreak by remember { mutableStateOf(0L) }
    var lastWorkoutDate by remember { mutableStateOf<String?>(null) }
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_DATE

    val filterOptions = listOf(7, 14, 30, -1)
    var selectedFilter by remember { mutableStateOf(30) }

    val canRestore = remember(lastWorkoutDate) {
        lastWorkoutDate?.let {
            val lastDate = LocalDate.parse(it, formatter)
            val daysMissed = today.toEpochDay() - lastDate.toEpochDay()
            daysMissed in 2..3
        } ?: false
    }

    LaunchedEffect(userId) {
        if (userId == null) {
            error = "User belum login"
            loading = false
            return@LaunchedEffect
        }

        try {
            val userSnapshot = db.collection("users").document(userId).get().await()
            dayStreak = userSnapshot.getLong("dayStreak") ?: 0L
            lastWorkoutDate = userSnapshot.getString("lastWorkoutDate")

            val snapshot = db.collection("users")
                .document(userId)
                .collection("workouts")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            workoutData = snapshot.documents.mapNotNull { doc ->
                val date = doc.id
                val cal = doc.getDouble("totalCalories")?.toFloat() ?: return@mapNotNull null
                date to cal
            }

        } catch (e: Exception) {
            error = e.localizedMessage
        }

        loading = false
    }

    val filteredHistory = remember(workoutData, selectedFilter) {
        if (selectedFilter == -1) workoutData else workoutData.takeLast(selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
            .padding(16.dp)
    ) {
        Text(
            text = "Grafik",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (error != null) {
            Text("Error: $error", color = Color.Red)
            return
        }

        if (workoutData.isEmpty()) {
            Text("Belum ada data aktivitas", modifier = Modifier.padding(top = 24.dp))
            return
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { useBarChart = !useBarChart }) {
                    Text(
                        text = if (useBarChart) "BarChart" else "LineChart",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                var expanded by remember { mutableStateOf(false) }

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            if (selectedFilter == -1) "Semua" else "$selectedFilter Hari",
                            color = Color.DarkGray
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        filterOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(if (it == -1) "Semua" else "$it Hari") },
                                onClick = {
                                    selectedFilter = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (useBarChart) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = filteredHistory.mapIndexed { idx, (_, cal) ->
                        BarEntry(idx.toFloat(), cal)
                    }

                    val dataSet = BarDataSet(entries, "Kalori").apply {
                        valueTextSize = 12f
                        color = Color(0xFFEF5350).toArgb()
                    }

                    val labels = filteredHistory.map { it.first.takeLast(5) }

                    chart.xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(labels)
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        textSize = 10f
                        setDrawGridLines(false)
                        labelRotationAngle = -45f
                    }

                    chart.axisLeft.textSize = 12f
                    chart.data = BarData(dataSet)
                    chart.invalidate()
                }
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = filteredHistory.mapIndexed { idx, (_, cal) ->
                        Entry(idx.toFloat(), cal)
                    }

                    val dataSet = LineDataSet(entries, "Kalori").apply {
                        valueTextSize = 12f
                        color = Color(0xFF42A5F5).toArgb()
                        setCircleColor(AndroidColor.BLACK)
                        circleRadius = 4f
                        lineWidth = 2f
                    }

                    val labels = filteredHistory.map { it.first.takeLast(5) }

                    chart.xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(labels)
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        textSize = 10f
                        setDrawGridLines(false)
                        labelRotationAngle = -45f
                    }

                    chart.axisLeft.textSize = 12f
                    chart.data = LineData(dataSet)
                    chart.invalidate()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val avgCalories by remember(workoutData) {
            derivedStateOf {
                if (workoutData.isNotEmpty()) workoutData.map { it.second }.average().toFloat() else 0f
            }
        }
        val recommendation = when {
            avgCalories < 200 -> "Yuk tingkatkan intensitas! Targetkan setidaknya 200 kalori per hari."
            avgCalories in 200f..400f -> "Lumayan stabil, pertahankan! Bisa coba naikkan sedikit demi progres lebih cepat."
            else -> "Keren! Kamu sangat aktif. Jangan lupa recovery juga ya."
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Rekomendasi", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(recommendation)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "🔥 Day Streak $dayStreak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (canRestore) {
                    Button(
                        onClick = {
                            val userRef = db.collection("users").document(userId!!)
                            val restoredValue = dayStreak
                            userRef.update(
                                mapOf(
                                    "dayStreak" to restoredValue,
                                    "lastWorkoutDate" to today.format(formatter)
                                )
                            )
                            dayStreak = restoredValue
                            lastWorkoutDate = today.format(formatter)
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Pulihkan", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
