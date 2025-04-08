package com.example.personalalertdevice

import com.example.personalalertdevice.Contacts.ContactsViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.ViewModelProvider
import com.example.personalalertdevice.Contacts.ContactsListScreen
import com.example.personalalertdevice.Contacts.ContactsScreen
import com.example.personalalertdevice.Health.HealthScreen
import com.example.personalalertdevice.Health.HistoryScreen
import com.example.personalalertdevice.Health.VitalsScreen
import com.example.personalalertdevice.Health.MedicalInfoScreen
import com.example.personalalertdevice.Health.MedicalViewModel
import com.example.personalalertdevice.Health.MedicalViewModelFactory
import com.example.personalalertdevice.Profile.ProfilePictureViewModel
import com.example.personalalertdevice.Profile.ProfilePictureViewModelFactory
import com.example.personalalertdevice.Profile.ProfileScreenMain
import com.example.personalalertdevice.Profile.ProfileScreen
import com.example.personalalertdevice.Profile.ProfileViewModel
import com.example.personalalertdevice.Profile.ProfileViewModelFactory
import com.google.firebase.firestore.FieldValue
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.navigation.NavHostController
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var medicalViewModel: MedicalViewModel
    private lateinit var googleAuthClient: GoogleAuthClient
    private val userRepository = UserRepository()
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

    private var userName: String = "User"
    private var profilePictureUrl: String? = null

    // Variable to store the last help request timestamp
    private var lastHelpRequestTimestamp: String? = null

    // Store NavController reference
    private var navControllerRef: NavHostController? = null

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialize Firebase Firestore
        val firestore = FirebaseFirestore.getInstance()

        //use custom factory to create ProfileViewModel
        val factory = ProfileViewModelFactory(firestore)
        profileViewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        googleAuthClient = GoogleAuthClient(this)

        // Load user data from UserDataDetails shared preference
        val (savedUserName, savedProfilePictureUrl) = loadUserData(this)
        userName = savedUserName
        profilePictureUrl = savedProfilePictureUrl

        val factoryMedical = MedicalViewModelFactory(firestore)
        medicalViewModel = ViewModelProvider(this, factoryMedical).get(MedicalViewModel::class.java)

        googleAuthClient = GoogleAuthClient(this)

        setContent {
            startPolling()
            val navController = rememberNavController()
            // Store NavController reference for use in other functions
            navControllerRef = navController

            val contactsViewModel = viewModel<ContactsViewModel>()
            val profilePictureViewModel: ProfilePictureViewModel =
                viewModel(factory = ProfilePictureViewModelFactory(firestore))
            val profileImageUrl = profilePictureViewModel.profileImageUrl.value
            val startDestination = if (googleAuthClient.isSignedIn()) {
                "MainScreen"
            } else {
                "LoginScreen"
            }
            val userId = firebaseAuth.currentUser?.uid ?: ""
            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    profilePictureViewModel.loadProfileImage(userId)
                }
            }

            // Listen for authentication state changes
            firebaseAuth.addAuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val fullName = firebaseUser.displayName ?: "User"
                    userName = fullName.split(" ").firstOrNull() ?: "User"
                    profilePictureUrl = firebaseUser.photoUrl?.toString()

                    // save user data to the shared preference
                    saveUserData(this@MainActivity, userName, profilePictureUrl)

                    // Save user data to Firestore after login, if not already saved
                    lifecycleScope.launch {
                        val result = userRepository.saveUserToFirestore()
                        if (result.isFailure) {
                            Log.e(
                                "FirestoreError",
                                "Failed to save user data: ${result.exceptionOrNull()}"
                            )
                        }
                    }
                }
            }

            NavHost(navController = navController, startDestination = startDestination) {
                val userId = firebaseAuth.currentUser?.uid ?: ""

                composable("MainScreen") {
                    MainScreen(
                        navController = navController,
                        userName = userName,
                        profilePictureUrl = profilePictureUrl,
                        viewModel = profilePictureViewModel,
                        userId = userId
                    )
                }
                composable("LoginScreen") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("MainScreen")
                        },
                        googleAuthClient = googleAuthClient
                    )
                }
                composable("ProfileScreenMain") {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    ProfileScreenMain(navController = navController, userId = userId)
                }
                composable("ProfileScreen") {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    ProfileScreen(navController = navController, userId = userId)
                }
                composable("HealthScreen") { HealthScreen(navController) }
                composable("ContactsScreen") { ContactsScreen(navController, contactsViewModel) }
                composable("HowToScreen") { HowToScreen(navController) }
                composable("HelpScreen") { HelpScreen(navController) }
                composable("VitalsScreen") { VitalsScreen(navController) }
                composable("HistoryScreen") { HistoryScreen(navController) }
                composable("MedicalInfoScreen") { MedicalInfoScreen(navController) }
                composable("ContactsListScreen") {
                    ContactsListScreen(navController, contactsViewModel)
                }
            }
        }
        lifecycleScope.launch {
            fetchAndUploadData()
        }
    }

    // Adafruit IO data
    data class AdafruitData(
        val id: String,
        val value: String,
        val feed_id: Int,
        val created_at: String
    )

    interface AdafruitService {
        @GET("feeds/{feedName}/data")
        suspend fun getData(
            @Path("feedName") feedName: String,
            @Header("X-AIO-Key") apiKey: String
        ): List<AdafruitData>
    }

    object RetrofitInstance {
        private const val BASE_URL = "x"

        val api: AdafruitService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AdafruitService::class.java)
        }
    }

    private fun startPolling() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                fetchAndUploadData()
                fetchAndUploadVitalsData()
                fetchAndUploadBatteryData()
                fetchAndUploadConnectionStatus()
                delay(3000)
            }
        }
    }

    private suspend fun fetchAndUploadData() {
        try {
            val apiKey = "x"
            val feedName = "x"

            val data = RetrofitInstance.api.getData(feedName, apiKey)

            if (data.isNullOrEmpty()) {
                Log.e("Adafruit", "No data found or data is empty!")
                return
            }

            val mostRecentItem = data.first()

            val utcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = try {
                utcDateFormat.parse(mostRecentItem.created_at)
            } catch (e: ParseException) {
                Log.e("Adafruit", "Error parsing date: ${e.message}")
                return
            }

            val estTimeZone = TimeZone.getTimeZone("America/New_York")
            val estDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            estDateFormat.timeZone = estTimeZone
            val formattedDate = estDateFormat.format(utcDate)

            val utf8Value = hexToUtf8(mostRecentItem.value)

            uploadToFirestore(
                AdafruitData(
                    id = mostRecentItem.id,
                    value = utf8Value,
                    feed_id = mostRecentItem.feed_id,
                    created_at = formattedDate
                )
            )

            checkForNewHelpRequest(utf8Value, formattedDate)

        } catch (e: UnknownHostException) {
            Log.e("Adafruit", "DNS Resolution Failed: ${e.message}")
        } catch (e: IOException) {
            Log.e("Adafruit", "Network Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Adafruit", "General Error: ${e.message}")
        }

        try {
            val url = URL("x")
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            Log.d("Network", "Response Code: ${connection.responseCode}")
        } catch (e: Exception) {
            Log.e("Network Error", "Failed to connect: ${e.message}")
        }
    }

    private suspend fun fetchAndUploadBatteryData() {
        try {
            val apiKey = "x"
            val feedName = "x"

            val data = RetrofitInstance.api.getData(feedName, apiKey)

            if (data.isNullOrEmpty()) {
                Log.e("Adafruit", "No Battery data found or data is empty!")
                return
            }

            val mostRecentItem = data.first()

            val utcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = try {
                utcDateFormat.parse(mostRecentItem.created_at)
            } catch (e: ParseException) {
                Log.e("Adafruit", "Error parsing date: ${e.message}")
                return
            }

            val estTimeZone = TimeZone.getTimeZone("America/New_York")
            val estDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            estDateFormat.timeZone = estTimeZone
            val formattedDate = estDateFormat.format(utcDate)

            val batteryPercentage = hexToUtf8(mostRecentItem.value)

            uploadBatteryToFirestore(
                mostRecentItem.id,
                batteryPercentage,
                formattedDate
            )
        } catch (e: Exception) {
            Log.e("Adafruit", "Error fetching Battery data: ${e.message}")
        }
    }

    private fun uploadBatteryToFirestore(id: String, batteryPercentage: String, timestamp: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Log.e("Adafruit", "User not authenticated!")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val documentRef = firestore.collection("Users").document(userId)

        val batteryData = hashMapOf(
            "id" to id,
            "percentage" to batteryPercentage,
            "last_updated" to timestamp
        )

        documentRef.set(mapOf("battery" to batteryData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Adafruit", "Battery data uploaded successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Adafruit", "Failed to upload battery data: ${e.message}")
            }
    }

    private fun checkForNewHelpRequest(message: String, timestamp: String) {
        if (message.lowercase().contains("help") && timestamp != lastHelpRequestTimestamp) {
            lastHelpRequestTimestamp = timestamp

            // calculate if the request is within the last 5 seconds
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val requestDate = sdf.parse(timestamp)
                val currentTime = Date()

                val diffInMillis = currentTime.time - requestDate.time
                val diffInSeconds = diffInMillis / 1000

                if (diffInSeconds <= 5) {
                    addEmergencyRequestToHistory(timestamp)

                    runOnUiThread {
                        navControllerRef?.navigate("HelpScreen")
                        Log.d("Emergency", "Navigating to help screen due to recent emergency request")
                    }
                }
            } catch (e: Exception) {
                Log.e("Emergency", "Error processing emergency request: ${e.message}")
            }
        }
    }

    private fun addEmergencyRequestToHistory(timestamp: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val firestore = FirebaseFirestore.getInstance()
        val documentRef = firestore.collection("Users").document(userId)

        documentRef.get()
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

                // timestamp as collection name
                documentRef.collection(timestamp).add(emergencyData)
                    .addOnSuccessListener {
                        Log.d("Emergency", "Emergency request added to history successfully")
                        CoroutineScope(Dispatchers.IO).launch {
                            uploadEmergencyViaWebhook(heartRate, temperature, timestamp, name, address)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Emergency", "Failed to add emergency request to history: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Emergency", "Failed to retrieve vitals data: ${e.message}")
            }
    }

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
        ${name} has triggered an emergency help request at ${timestamp}.
    
        Address: ${address}
        Trigger Method: speech
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

    private fun uploadToFirestore(latestData: AdafruitData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Log.e("Adafruit", "User not authenticated!")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val documentRef = firestore.collection("Users").document(userId)

        val dataMap = hashMapOf(
            "id" to latestData.id,
            "value" to latestData.value,
            "created at" to latestData.created_at
        )

        documentRef.set(mapOf("send help history" to dataMap), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Adafruit", "Data uploaded successfully to user document!")
            }
            .addOnFailureListener { e ->
                Log.e("Adafruit", "Failed to upload data: ${e.message}")
            }
    }

    private suspend fun fetchAndUploadVitalsData() {
        try {
            val apiKey = "x"
            val feedName = "x"

            val data = RetrofitInstance.api.getData(feedName, apiKey)

            if (data.isNullOrEmpty()) {
                Log.e("Adafruit", "No Vitals data found or data is empty!")
                return
            }

            val mostRecentItem = data.first()

            val utcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = try {
                utcDateFormat.parse(mostRecentItem.created_at)
            } catch (e: ParseException) {
                Log.e("Adafruit", "Error parsing date: ${e.message}")
                return
            }

            val estTimeZone = TimeZone.getTimeZone("America/New_York")
            val estDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            estDateFormat.timeZone = estTimeZone
            val formattedDate = estDateFormat.format(utcDate)

            val utf8Value = hexToUtf8(mostRecentItem.value)
            val values = utf8Value.split(",")

            if (values.size < 3) {
                Log.e("Adafruit", "Invalid Vitals data format!")
                return
            }

            val heartRate = values[0]
            val spo2 = values[1]
            val temperature = values[2]

            uploadVitalsToFirestore(
                mostRecentItem.id,
                heartRate,
                spo2,
                temperature,
                formattedDate
            )
        } catch (e: Exception) {
            Log.e("Adafruit", "Error fetching Vitals data: ${e.message}")
        }
    }

    private fun uploadVitalsToFirestore(id: String, heartRate: String, spo2: String, temperature: String, timestamp: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Log.e("Adafruit", "User not authenticated!")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val documentRef = firestore.collection("Users").document(userId)

        val vitalsData = hashMapOf(
            "id" to id,
            "heart rate" to heartRate,
            "spo2" to spo2,
            "temperature" to temperature,
            "created at" to timestamp
        )

        documentRef.set(mapOf("vitals history" to vitalsData), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Adafruit", "Vitals data uploaded successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Adafruit", "Failed to upload vitals data: ${e.message}")
            }
    }

    private suspend fun fetchAndUploadConnectionStatus() {
        try {
            val apiKey = "x"
            val feedName = "x"

            val data = RetrofitInstance.api.getData(feedName, apiKey)

            if (data.isNullOrEmpty()) {
                Log.e("Adafruit", "No connection status data found or data is empty!")
                return
            }

            val mostRecentItem = data.first()

            val utcDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = try {
                utcDateFormat.parse(mostRecentItem.created_at)
            } catch (e: ParseException) {
                Log.e("Adafruit", "Error parsing date: ${e.message}")
                return
            }

            val estTimeZone = TimeZone.getTimeZone("America/New_York")
            val estDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            estDateFormat.timeZone = estTimeZone
            val formattedDate = estDateFormat.format(utcDate)

            val utf8Value = hexToUtf8(mostRecentItem.value)

            val status = utf8Value

            uploadConnectionStatusToFirestore(
                status,
                formattedDate
            )
        } catch (e: Exception) {
            Log.e("Adafruit", "Error fetching connection status data: ${e.message}")
        }
    }

    private var lastStatusCheckTime: Long = 0

    private fun uploadConnectionStatusToFirestore(status: String, timestamp: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("Adafruit", "User not authenticated!")
            return
        }

        // Check if 5 seconds have passed since last status check
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStatusCheckTime < 5000) {
            Log.d("Adafruit", "Skipping status check - less than 5 seconds since last check")
            return
        }
        lastStatusCheckTime = currentTime

        val firestore = FirebaseFirestore.getInstance()
        val documentRef = firestore.collection("Users").document(userId)

        // do only every 5 seconds
        //check connection status
        documentRef.get()
            .addOnSuccessListener { document ->
                val lastTimestamp = document.getString("device connection status.created at")

                // if timestamp is new → "connected", else → "disconnected"
                val newStatus = if (lastTimestamp != timestamp) "connected" else "disconnected"

                val connectionStatus = hashMapOf(
                    "device status" to newStatus,
                    "created at" to timestamp,
                    "last_checked" to FieldValue.serverTimestamp() // debug
                )

                documentRef.set(mapOf("device connection status" to connectionStatus), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Adafruit", "Status: $newStatus (Timestamp: $timestamp) [Checked at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}]")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Adafruit", "Failed to update status", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Adafruit", "Failed to fetch previous timestamp", e)
            }
    }

    private fun hexToUtf8(hexString: String): String {
        val byteArray = ByteArray(hexString.length / 2)
        for (i in hexString.indices step 2) {
            byteArray[i / 2] = ((hexString[i].toString() + hexString[i + 1]).toInt(16) and 0xFF).toByte()
        }
        return String(byteArray, Charsets.UTF_8)
    }
}