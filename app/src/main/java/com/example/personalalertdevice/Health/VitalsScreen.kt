package com.example.personalalertdevice.Health

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val heartRateBPMHistory = remember { mutableStateListOf<Float>() }
    val spO2 = remember { mutableStateOf("Loading...") }
    val spO2History = remember { mutableStateListOf<Float>() }
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

    val avgBPM = remember { derivedStateOf {
        heartRateBPMHistory.averageOrNull()?.let { "%.0f bpm".format(it) } ?: "N/A"
    } }
    val highBPM = remember { derivedStateOf {
        heartRateBPMHistory.maxOrNull()?.let { "%.0f bpm".format(it) } ?: "N/A"
    } }
    val lowBPM = remember { derivedStateOf {
        heartRateBPMHistory.minOrNull()?.let { "%.0f bpm".format(it) } ?: "N/A"
    } }

    val avgSPO2 = remember { derivedStateOf {
        spO2History.averageOrNull()?.let { "%.0f%%".format(it) } ?: "N/A"
    } }
    val highSPO2 = remember { derivedStateOf {
        spO2History.maxOrNull()?.let { "%.0f%%".format(it) } ?: "N/A"
    } }
    val lowSPO2 = remember { derivedStateOf {
        spO2History.minOrNull()?.let { "%.0f%%".format(it) } ?: "N/A"
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
                    spO2.value = "Error"
                    deviceStatus.value = "error"
                    errorMessage.value = error.message
                    return@addSnapshotListener
                }

                documentSnapshot?.let { document ->

                    val connectionStatusMap = document.get("device connection status") as? Map<*, *>

                    val status = connectionStatusMap?.get("device status")?.toString()?.lowercase() ?: "unknown"
                    deviceStatus.value = status

                    when (status) {
                        "disconnected" -> {
                            bodyTemperature.value = "Disconnected"
                            heartRateBPM.value = "Disconnected"
                            spO2.value = "Disconnected"
                            errorMessage.value = "Device disconnected"
                            temperatureHistory.clear()
                            heartRateBPMHistory.clear()
                            spO2History.clear()
                        }
                        "connected" -> {
                            // Fetch vitals history only if connected
                            val vitalsHistory = document.get("vitals history") as? Map<*, *>
                            val temperature = vitalsHistory?.get("temperature")?.toString() ?: "N/A"
                            val heartRate = vitalsHistory?.get("heart rate")?.toString() ?: "N/A"
                            val bloodoxygen = vitalsHistory?.get("spo2")?.toString() ?: "N/A"
                            val celsius = temperature.toDoubleOrNull()

                            // temperature
                            if (celsius != null) {
                                val fahrenheit = (celsius * 9 / 5) + 32
                                bodyTemperature.value = "%.2f°F".format(fahrenheit)

                                if (temperatureHistory.size >= 10) {
                                    temperatureHistory.removeAt(0)
                                }
                                temperatureHistory.add(fahrenheit.toFloat())
                            } else {
                                bodyTemperature.value = "N/A"
                            }

                            // heart rate
                            val heartRateValue = heartRate.toFloatOrNull()
                            if (heartRateValue != null) {
                                heartRateBPM.value = "%.0f bpm".format(heartRateValue)

                                if (heartRateBPMHistory.size >= 10) {
                                    heartRateBPMHistory.removeAt(0)
                                }
                                heartRateBPMHistory.add(heartRateValue)
                            } else {
                                heartRateBPM.value = "N/A"
                            }

                            // SpO2
                            val spo2Value = bloodoxygen.toFloatOrNull()
                            if (spo2Value != null) {
                                spO2.value = "%.0f%%".format(spo2Value)

                                if (spO2History.size >= 10) {
                                    spO2History.removeAt(0)
                                }
                                spO2History.add(spo2Value)
                            } else {
                                spO2.value = "N/A"
                            }

                            errorMessage.value = null
                        }
                        else -> {
                            bodyTemperature.value = "Unknown"
                            heartRateBPM.value = "Unknown"
                            spO2.value = "Unknown"
                            errorMessage.value = "Device status not available"
                        }
                    }
                }
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5) // Light gray background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigate("HealthScreen") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
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

                val statusText = when (deviceStatus.value) {
                    "connected" -> "CONNECTED"
                    "disconnected" -> "DISCONNECTED"
                    "loading" -> "LOADING..."
                    "error" -> "ERROR"
                    else -> "UNKNOWN"
                }

                val statusColor = when (deviceStatus.value) {
                    "connected" -> Color(0xFF4CAF50) // Green
                    "disconnected" -> Color(0xFFF44336) // Red
                    "loading" -> Color(0xFF2196F3) // Blue
                    "error" -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFF9E9E9E) // Gray
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Vitals Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Determine values based on connection status
                val showInvalidData = deviceStatus.value != "connected"

                VitalsSectionCard(
                    title = "Heart Rate",
                    icon = painterResource(id = R.drawable.heart),
                    current = if (showInvalidData) "Invalid" else heartRateBPM.value,
                    avg = if (showInvalidData) "Invalid" else avgBPM.value,
                    highLow = if (showInvalidData) "Invalid" else "${highBPM.value} / ${lowBPM.value}",
                    cardColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    iconTint = Color(0xFFE53935)
                )

                VitalsSectionCard(
                    title = "Body Temperature",
                    icon = painterResource(id = R.drawable.temp),
                    current = if (showInvalidData) "Invalid" else bodyTemperature.value,
                    avg = if (showInvalidData) "Invalid" else avgTemperature.value,
                    highLow = if (showInvalidData) "Invalid" else "${highTemperature.value} / ${lowTemperature.value}",
                    cardColor = Color(0xFFFFB74D).copy(alpha = 0.2f),
                    iconTint = Color(0xFFFF9800)
                )

                VitalsSectionCard(
                    title = "Blood Oxygen",
                    icon = painterResource(id = R.drawable.bo2),
                    current = if (showInvalidData) "Invalid" else spO2.value,
                    avg = if (showInvalidData) "Invalid" else avgSPO2.value,
                    highLow = if (showInvalidData) "Invalid" else "${highSPO2.value} / ${lowSPO2.value}",
                    cardColor = Color(0xFF90CAF9).copy(alpha = 0.2f),
                    iconTint = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun VitalsSectionCard(
    title: String,
    icon: Painter,
    current: String,
    avg: String,
    highLow: String,
    cardColor: Color,
    iconTint: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = icon,
                            contentDescription = "$title Icon",
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Current Reading",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Text(
                text = current,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    current.equals("Invalid", ignoreCase = true) -> Color.Red
                    current.equals("Unknown", ignoreCase = true) -> Color(0xFFFF9800)
                    current.equals("Error", ignoreCase = true) -> Color(0xFFF44336)
                    current.equals("Loading...", ignoreCase = true) -> Color(0xFF2196F3)
                    else -> iconTint
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Divider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "24-HR Average",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = avg,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (avg == "N/A" || avg == "Invalid") Color.Gray else Color.DarkGray
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "24-HR High / Low",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = highLow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (highLow == "N/A" || highLow == "Invalid") Color.Gray else Color.DarkGray
                    )
                }
            }
        }
    }
}

fun List<Float>.averageOrNull(): Float? = if (isNotEmpty()) average().toFloat() else null