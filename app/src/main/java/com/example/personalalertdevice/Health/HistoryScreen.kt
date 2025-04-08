package com.example.personalalertdevice.Health

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


data class EmergencyRecord(
    val timestamp: String,
    val trigger: String,
    val heartRate: String,
    val temperature: String,
    val name: String,
    val address: String
)

@Composable
fun HistoryScreen(navController: NavController) {
    val emergencyRecords = remember { mutableStateOf<List<EmergencyRecord>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = true) {
        fetchEmergencyRecords(
            onSuccess = { records ->
                emergencyRecords.value = records
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
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

        when {
            isLoading.value -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        color = Color.Red,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            errorMessage.value != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Error: ${errorMessage.value}",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                }
            }
            emergencyRecords.value.isEmpty() -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No emergency records found",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(emergencyRecords.value) { record ->
                        EmergencyRecordCard(record)
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyRecordCard(record: EmergencyRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency Alert",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Trigger: ${record.trigger}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Data and Time: ${record.timestamp}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Heart Rate",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${record.heartRate} BPM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Temperature",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${record.temperature} °F",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Name: ${record.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Location: ${record.address}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

suspend fun fetchEmergencyRecords(
    onSuccess: (List<EmergencyRecord>) -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("User not authenticated")

        val firestore = FirebaseFirestore.getInstance()
        val emergencyRecordsRef = firestore.collection("Users")
            .document(userId)
            .collection("emergency_records")

        val querySnapshot = emergencyRecordsRef.get().await()
        val records = mutableListOf<EmergencyRecord>()

        for (document in querySnapshot.documents) {
            val timestamp = document.getString("timestamp") ?: document.id
            val trigger = document.getString("trigger") ?: "Unknown"
            val heartRate = document.getString("heart_rate") ?: "N/A"
            val temperature = document.getString("temperature") ?: "N/A"
            val name = document.getString("name") ?: "N/A"
            val address = document.getString("address") ?: "N/A"

            records.add(
                EmergencyRecord(
                    timestamp = timestamp,
                    trigger = trigger,
                    heartRate = heartRate,
                    temperature = temperature,
                    name = name,
                    address = address
                )
            )
        }

        onSuccess(records.sortedByDescending { it.timestamp }) // Newest first
    } catch (e: Exception) {
        Log.e("FetchEmergencyRecords", "Error fetching emergency records", e)
        onFailure(e.localizedMessage ?: "Unknown error")
    }
}


