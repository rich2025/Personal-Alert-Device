package com.example.personalalertdevice

import android.bluetooth.BluetoothHidDevice
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.personalalertdevice.Contacts.ContactsViewModel
import com.example.personalalertdevice.Health.MedicalViewModel
import com.example.personalalertdevice.Health.MedicalViewModelFactory
import com.example.personalalertdevice.MainActivity.AdafruitData
import com.example.personalalertdevice.MainActivity.AdafruitService
import com.example.personalalertdevice.Profile.ProfilePictureViewModel
import com.example.personalalertdevice.Profile.ProfileViewModel
import com.example.personalalertdevice.Profile.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response
import java.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


// Composable for main screen
@Composable
fun MainScreen(
    navController: NavController,
    userName: String,
    profilePictureUrl: String?,
    viewModel: ProfilePictureViewModel,
    userId: String
) {
    LaunchedEffect(userId) {
        viewModel.loadProfileImage(userId)
    }

    // check device connection status and change border color accordingly
    var deviceStatus by remember { mutableStateOf("disconnected") }
    val borderColor = if (deviceStatus == "disconnected") Color.Red else Color.Green

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        while (true) {
            db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val statusMap = snapshot.get("device connection status") as? Map<*, *>
                        val status = statusMap?.get("device status") as? String
                        if (status == "connected" || status == "disconnected") {
                            deviceStatus = status

                            // Check if device is disconnected and interval has passed
                            if (deviceStatus == "disconnected") {
                                val currentTime = System.currentTimeMillis()

                                // Only send notification if 5 minutes have passed since last one
                                if (currentTime - lastNotificationTime >= NOTIFICATION_INTERVAL) {
                                    snapshot.getString("full name")?.let { name ->
                                        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

                                        CoroutineScope(Dispatchers.IO).launch {
                                            sendDeviceDisconnectionWebhook(
                                                timestamp,
                                                name
                                            )
                                        }
                                        lastNotificationTime = currentTime
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    deviceStatus = "disconnected"
                }
            delay(3000)
        }
    }

    val uploadedProfilePictureUrl = viewModel.profileImageUrl.value

//    val firestore = FirebaseFirestore.getInstance()
//    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(firestore))
//
//    val medicalViewModel: MedicalViewModel = viewModel(factory = MedicalViewModelFactory(firestore))
//
//    val contactsViewModel: ContactsViewModel = viewModel()
//    val contacts = contactsViewModel.designatedContacts
//
//    LaunchedEffect(userId) {
//        profileViewModel.loadProfileData(userId)
//        medicalViewModel.loadMedicalData(userId)
//    }
//
//    val profileData = profileViewModel.profileData.value
//    val medicalData = medicalViewModel.medicalData.value
//
//    val name = profileData?.get("full name") ?: ""
//    val age = profileData?.get("age") ?: ""
//    val gender = profileData?.get("gender") ?: ""
//    val weight = profileData?.get("weight") ?: ""
//    val height = profileData?.get("height") ?: ""
//    val address = profileData?.get("address") ?: ""
//
//    val allergies = medicalData?.get("allergies") ?: ""
//    val medications = medicalData?.get("medications") ?: ""
//    val donor = medicalData?.get("donor") ?: ""
//    val conditions = medicalData?.get("conditions") ?: ""

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sign Out button
        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate("LoginScreen") {
                    popUpTo("MainScreen") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .width(200.dp)
                .height(55.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffde4526))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                if (profilePictureUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Retrieve user name and profile picture
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.padding(vertical = 70.dp)
                    .offset(y = 17.dp)
            )

            if (uploadedProfilePictureUrl != null) {
                AsyncImage(
                    model = uploadedProfilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(170.dp)
                        .offset(y = (-20).dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(170.dp)
                        .offset(y = (-20).dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Navigation Button Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
                    .offset(y = -15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            navController.navigate("ProfileScreenMain")
                        },
                        modifier = Modifier.size(175.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.profile_icon),
                                contentDescription = "Profile Icon",
                                modifier = Modifier.size(50.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 43.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
                            )
                        }
                    }
                    Button(
                        onClick = {
                            navController.navigate("HealthScreen")
                        },
                        modifier = Modifier.size(175.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.health_icon),
                                contentDescription = "Health Icon",
                                modifier = Modifier.size(57.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Health",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 43.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            navController.navigate("ContactsScreen")
                        },
                        modifier = Modifier.size(175.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.contacts_icon),
                                contentDescription = "Contacts Icon",
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Contact",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black,
                                maxLines = 1
                            )
                        }
                    }
                    Button(
                        onClick = {
                            navController.navigate("HowToScreen")
                        },
                        modifier = Modifier
                            .size(175.dp)
                            .border(4.dp, borderColor, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd7c0ed))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.how_to_icon),
                                contentDescription = "How To Icon",
                                modifier = Modifier.size(63.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Device",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 37.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }
                HoldButtonWithProgress(
                    onCompleted = {
                        navController.navigate("HelpScreen")
                        addEmergencyRequestToHistory()
                                  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}

    // Composable for holdable button
    @Composable
    fun HoldButtonWithProgress(
        onCompleted: () -> Unit,
        modifier: Modifier = Modifier,
        holdDuration: Long = 20000L,
        progressColor: Color = Color(0xFF8B0000),
        backgroundColor: Color = Color.Red
    ) {
        var progress by remember { mutableFloatStateOf(0f) }
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "")
        var isPressed by remember { mutableStateOf(false) }
        var helpCalled by remember { mutableStateOf(false) } // Track if help was called
        val scope = rememberCoroutineScope()

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            scope.launch {
                                while (isPressed && progress < 1f && !helpCalled) {
                                    delay(10)
                                    progress += 0.01f * (10f / holdDuration * 1000f)
                                }
                                if (progress >= 1f) {
                                    helpCalled = true // Mark as help called
                                    onCompleted()
                                }
                            }
                            tryAwaitRelease()
                            isPressed = false
                            scope.launch {
                                if (!helpCalled) {
                                    while (!isPressed && progress > 0f) {
                                        delay(10)
                                        progress -= 0.05f * (10f / holdDuration * 1000f)
                                    }
                                }
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                // Progress bar box
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(progressColor)
                )

                Text(
                    text = if (helpCalled) "HELP CALLED" else "HOLD FOR HELP",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }

private fun addEmergencyRequestToHistory() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    val userDocRef = firestore.collection("Users").document(userId)

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val timestamp = sdf.format(Date())

    userDocRef.get()
        .addOnSuccessListener { document ->
            val heartRate = document.getString("vitals history.heart rate") ?: "Unknown"
            val temperature = document.getString("vitals history.temperature") ?: "Unknown"
            val name = document.getString("full name") ?: "Unknown"
            val address = document.getString("address") ?: "Unknown"

            val emergencyData = hashMapOf(
                "timestamp" to timestamp,
                "created_at" to FieldValue.serverTimestamp(),
                "trigger" to "app",
                "heart_rate" to heartRate,
                "temperature" to temperature,
                "name" to name,
                "address" to address
            )

            val emergencyRecordsRef = userDocRef
                .collection("emergency_records")
                .document(timestamp)

            emergencyRecordsRef.set(emergencyData)
                .addOnSuccessListener {
                    Log.d("Emergency", "Emergency request added to emergency_records successfully")
                    CoroutineScope(Dispatchers.IO).launch {
                        uploadEmergencyViaWebhook(heartRate, temperature, timestamp, name, address)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Emergency", "Failed to add emergency request: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            Log.e("Emergency", "Failed to retrieve user data: ${e.message}")
        }
}


// use twilio for multiple sms messages
private suspend fun uploadEmergencyViaWebhook(
    heartRate: String,
    temperature: String,
    timestamp: String,
    name: String,
    address: String
) {
    val client = OkHttpClient()

    val temperatureInCelsius = temperature.toDoubleOrNull() ?: 0.0
    val temperatureInFahrenheit = (temperatureInCelsius * 9 / 5) + 32

    val formattedData = """
    EMERGENCY ALERT!
    
    ${name} has triggered an emergency help request at ${timestamp}.
    
    Address: ${address}
    Trigger Method: app
    Current Heart Rate: ${heartRate} bpm
    Current Skin Temperature: ${temperatureInFahrenheit} °F
""".trimIndent()

    val jsonPayload = JSONObject(mapOf("value" to formattedData)).toString()

    val request = Request.Builder()
        .url("x")
        .addHeader("Content-Type", "application/json")
        .post(jsonPayload.toRequestBody("application/json".toMediaType()))
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d("Webhook", "Webhook triggered successfully!")
        } else {
            Log.e("Webhook", "Webhook failed (${response.code}): ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.e("Webhook", "Error triggering webhook: ${e.message}")
    }
}

// connections status notification

private var lastNotificationTime = 0L
private const val NOTIFICATION_INTERVAL = 5 * 60 * 1000

private suspend fun sendDeviceDisconnectionWebhook(
    timestamp: String,
    name: String
) {
    val client = OkHttpClient()

    val formattedData = """
    ALERT: ${name}'s Personal Alert Device is DISCONNECTED as of ${timestamp}.
    
    Troubleshooting:
    
    1) Check device Bluetooth connection
    
    2) Check device power
    
    3) Check phone network connectivity
    
    """.trimIndent()

    val jsonPayload = JSONObject(mapOf("value" to formattedData)).toString()

    val request = Request.Builder()
        .url("x")
        .addHeader("Content-Type", "application/json")
        .post(jsonPayload.toRequestBody("application/json".toMediaType()))
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d("Webhook", "Device disconnection webhook triggered successfully!")
        } else {
            Log.e("Webhook", "Device disconnection webhook failed (${response.code}): ${response.body?.string()}")
        }
    } catch (e: Exception) {
        Log.e("Webhook", "Error triggering device disconnection webhook: ${e.message}")
    }
}