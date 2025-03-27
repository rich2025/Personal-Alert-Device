package com.example.personalalertdevice.Health

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.personalalertdevice.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun VitalsScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val firestore = FirebaseFirestore.getInstance()

    val bodyTemperature = remember { mutableStateOf("Loading...") }
    val temperatureHistory = remember { mutableStateListOf<Float>() }
    val heartRateBPM = remember { mutableStateOf("Loading...") }
    val deviceStatus = remember { mutableStateOf("loading") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val avgTemperature = remember { derivedStateOf {
        temperatureHistory.averageOrNull()?.let { "%.2f°F".format(it) } ?: "N/A"
    } }
    val highTemperature = remember { derivedStateOf {
        temperatureHistory.maxOrNull()?.let { "%.2f°F".format(it) } ?: "N/A"
    } }
    val lowTemperature = remember { derivedStateOf {
        temperatureHistory.minOrNull()?.let { "%.2f°F".format(it) } ?: "N/A"
    } }

    // Real-time Firestore listener
    LaunchedEffect(userId) {
        firestore.collection("Users")
            .document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("VitalsScreen", "Error fetching data: ${error.message}")
                    bodyTemperature.value = "Error"
                    heartRateBPM.value = "Error"
                    deviceStatus.value = "error"
                    errorMessage.value = error.message
                    return@addSnapshotListener
                }

                documentSnapshot?.let { document ->
                    // Get the connection status map
                    val connectionStatusMap = document.get("device connection status") as? Map<*, *>

                    // Extract device status from the map
                    val status = connectionStatusMap?.get("device status")?.toString()?.lowercase() ?: "unknown"
                    deviceStatus.value = status

                    when (status) {
                        "disconnected" -> {
                            bodyTemperature.value = "Disconnected"
                            heartRateBPM.value = "Disconnected"
                            errorMessage.value = "Device disconnected"
                            temperatureHistory.clear()
                        }
                        "connected" -> {
                            // Fetch vitals history only if connected
                            val vitalsHistory = document.get("vitals history") as? Map<*, *>
                            val temperature = vitalsHistory?.get("temperature")?.toString() ?: "N/A"
                            val heartRate = vitalsHistory?.get("heart rate")?.toString() ?: "N/A"
                            val celsius = temperature.toDoubleOrNull()

                            // Process temperature
                            if (celsius != null) {
                                val fahrenheit = (celsius * 9 / 5) + 32
                                bodyTemperature.value = "%.2f°F".format(fahrenheit)

                                // Maintain last 10 readings
                                if (temperatureHistory.size >= 10) {
                                    temperatureHistory.removeAt(0)
                                }
                                temperatureHistory.add(fahrenheit.toFloat())
                            } else {
                                bodyTemperature.value = "N/A"
                            }

                            // Update heart rate
                            heartRateBPM.value = if (heartRate != "N/A") "$heartRate bpm" else "N/A"
                            errorMessage.value = null
                        }
                        else -> {
                            bodyTemperature.value = "Unknown"
                            heartRateBPM.value = "Unknown"
                            errorMessage.value = "Device status not available"
                        }
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Return Button
        Button(
            onClick = { navController.navigate("HealthScreen") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 32.dp, bottom = 16.dp, start = 9.dp)
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
                    text = "Health Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Show connection status with improved display
        val statusText = when (deviceStatus.value) {
            "connected" -> "CONNECTED"
            "disconnected" -> "DISCONNECTED"
            "loading" -> "LOADING..."
            "error" -> "ERROR"
            else -> "UNKNOWN STATUS"
        }

        val statusColor = when (deviceStatus.value) {
            "connected" -> Color(0xFF4CAF50) // Green
            "disconnected" -> Color(0xFFF44336) // Red
            "loading" -> Color(0xFF2196F3) // Blue
            "error" -> Color(0xFFFF9800) // Orange
            else -> Color(0xFF9E9E9E) // Gray
        }

        Text(
            text = "Device Status: $statusText",
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier.padding(bottom = 15.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 0.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Determine values based on connection status
            val showInvalidData = deviceStatus.value != "connected"

            VitalsSection(
                title = "Heart Rate",
                icon = painterResource(id = R.drawable.heart),
                current = if (showInvalidData) "Invalid" else heartRateBPM.value,
                avg = if (showInvalidData) "Invalid" else "75 bpm",
                highLow = if (showInvalidData) "Invalid" else "95 bpm / 60 bpm"
            )

            VitalsSection(
                title = "Skin Body Temperature",
                icon = painterResource(id = R.drawable.temp),
                current = if (showInvalidData) "Invalid" else bodyTemperature.value,
                avg = if (showInvalidData) "Invalid" else avgTemperature.value,
                highLow = if (showInvalidData) "Invalid" else "${highTemperature.value} / ${lowTemperature.value}"
            )

            VitalsSection(
                title = "Blood Oxygen",
                icon = painterResource(id = R.drawable.bo2),
                current = if (showInvalidData) "Invalid" else "98%",
                avg = if (showInvalidData) "Invalid" else "97%",
                highLow = if (showInvalidData) "Invalid" else "99% / 95%"
            )
        }
    }
}

@Composable
fun VitalsSection(
    title: String,
    icon: Painter,
    current: String,
    avg: String,
    highLow: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Divider(
                color = Color.Gray,
                thickness = 2.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = icon,
                    contentDescription = "$title Icon",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(start = 10.dp, end = 0.dp, bottom = 5.dp)
                )
                Spacer(modifier = Modifier.width(50.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row {
                        Text(
                            text = "Current: ",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1d2026)
                        )
                        Text(
                            text = current,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                current.equals("Invalid", ignoreCase = true) -> Color.Red
                                current.equals("Unknown", ignoreCase = true) -> Color(0xFFFF9800)
                                current.equals("Error", ignoreCase = true) -> Color(0xFFF44336)
                                current.equals("Loading...", ignoreCase = true) -> Color(0xFF2196F3)
                                else -> Color.DarkGray
                            }
                        )
                    }
                    Row {
                        Text(
                            text = "24-HR Avg: ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1d2026)
                        )
                        Text(
                            text = avg,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (avg == "N/A") Color.Gray else Color.DarkGray
                        )
                    }
                    Row {
                        Text(
                            text = "24-HR Hi/Lo: ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1d2026)
                        )
                        Text(
                            text = highLow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (highLow == "N/A") Color.Gray else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

fun List<Float>.averageOrNull(): Float? = if (isNotEmpty()) average().toFloat() else null