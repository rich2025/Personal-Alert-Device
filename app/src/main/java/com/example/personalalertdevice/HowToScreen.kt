package com.example.personalalertdevice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceStatus(
    val connectionStatus: String = "Unknown",
    val lastChecked: Timestamp? = null,
    val batteryPercentage: Int = 0,
    val batteryLastUpdated: Timestamp? = null
)

fun String.capitalize(locale: Locale): String {
    return if (this.isEmpty()) {
        ""
    } else {
        this.substring(0, 1).uppercase(locale) + this.substring(1)
    }
}

@Composable
fun HowToScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val firestore = FirebaseFirestore.getInstance()

    var deviceStatus by remember { mutableStateOf<DeviceStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val connectionStatusMap = document.get("device connection status") as? Map<*, *>
                        val status = connectionStatusMap?.get("device status")?.toString()?.lowercase() ?: "unknown"
                        val lastChecked = connectionStatusMap?.get("last_checked") as? Timestamp

                        // Get battery data
                        val batteryMap = document.get("battery") as? Map<*, *>
                        val batteryVoltage = batteryMap?.get("percentage")?.toString()?.toFloatOrNull() ?: 0f
                        val batteryPercentage = ((batteryVoltage / 3.3f) * 100).toInt().coerceIn(0, 100)
                        val batteryLastUpdated = batteryMap?.get("last_updated") as? Timestamp

                        deviceStatus = DeviceStatus(
                            connectionStatus = status.capitalize(Locale.getDefault()),
                            lastChecked = lastChecked,
                            batteryPercentage = batteryPercentage,
                            batteryLastUpdated = batteryLastUpdated
                        )

                    } else {
                        loadError = "No device data found"
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    loadError = "Failed to load data: ${e.message}"
                    isLoading = false
                }
        } else {
            loadError = "User not authenticated"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Return Button
        Button(
            onClick = { navController.navigate("MainScreen") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(45.dp)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "RETURN",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Main Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            loadError != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFDE7E7)
                    )
                ) {
                    Text(
                        text = loadError ?: "Unknown error",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                deviceStatus?.let { status ->
                    // Connection Status Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = when (status.connectionStatus.lowercase()) {
                                "connected" -> Color(0xFFE3F2FD)
                                "disconnected" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = when (status.connectionStatus.lowercase()) {
                                                "connected" -> Color(0xFF2196F3)
                                                "disconnected" -> Color(0xFFE57373)
                                                else -> Color.Gray
                                            },
                                            shape = CircleShape
                                        )
                                ) {
                                    Text(
                                        text = if (status.connectionStatus.lowercase() == "connected") "ON" else "OFF",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "Connection Status",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = status.connectionStatus,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (status.connectionStatus.lowercase()) {
                                            "connected" -> Color(0xFF2196F3)
                                            "disconnected" -> Color(0xFFE57373)
                                            else -> Color.Gray
                                        }
                                    )
                                }
                            }

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )

                            Text(
                                text = "Last Checked: ${status.lastChecked?.toDate()?.let { formatDate(it) } ?: "Unknown"}",
                                fontSize = 16.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    // Battery Status Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                status.connectionStatus.lowercase() == "disconnected" -> Color(0xFFF5F5F5) // Gray for invalid
                                status.batteryPercentage >= 60 -> Color(0xFFE8F5E9)
                                status.batteryPercentage >= 30 -> Color(0xFFFFF8E1)
                                else -> Color(0xFFFBE9E7)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = when {
                                                status.connectionStatus.lowercase() == "disconnected" -> Color.Gray
                                                status.batteryPercentage >= 60 -> Color(0xFF4CAF50)
                                                status.batteryPercentage >= 30 -> Color(0xFFFF9800)
                                                else -> Color(0xFFF44336)
                                            },
                                            shape = CircleShape
                                        )
                                ) {
                                    Text(
                                        text = if (status.connectionStatus.lowercase() == "disconnected")
                                            "?" else "${status.batteryPercentage}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "Battery Status",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (status.connectionStatus.lowercase() == "disconnected")
                                            "Invalid" else "${status.batteryPercentage}%",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            status.connectionStatus.lowercase() == "disconnected" -> Color.Gray
                                            status.batteryPercentage >= 60 -> Color(0xFF4CAF50)
                                            status.batteryPercentage >= 30 -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                }
                            }

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )

                            if (status.connectionStatus.lowercase() == "disconnected") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Battery information unavailable while device is disconnected",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(status.batteryPercentage / 100f)
                                            .height(20.dp)
                                            .background(
                                                when {
                                                    status.batteryPercentage >= 60 -> Color(0xFF4CAF50)
                                                    status.batteryPercentage >= 30 -> Color(0xFFFF9800)
                                                    else -> Color(0xFFF44336)
                                                },
                                                RoundedCornerShape(10.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return formatter.format(date)
}