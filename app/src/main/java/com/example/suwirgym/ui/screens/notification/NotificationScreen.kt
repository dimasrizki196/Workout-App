package com.example.suwirgym.ui.screens.notification

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
import com.google.firebase.firestore.Query

@Composable
fun NotificationScreen(userId: String?) {
    val db = FirebaseFirestore.getInstance()
    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorMessage = "Gagal memuat notifikasi: ${error.message}"
                        notifications = emptyList()
                    } else if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull {
                            it.data
                        }
                        errorMessage = null
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat Notifikasi",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (notifications.isEmpty()) {
            Text(
                text = "Belum ada notifikasi",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            notifications.forEach { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = notif["title"] as? String ?: "Tidak ada judul",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = notif["message"] as? String ?: "")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = timestampToString(notif["timestamp"] as? Long ?: 0L),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun timestampToString(time: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
    return if (time > 0L) sdf.format(java.util.Date(time)) else "-"
}
