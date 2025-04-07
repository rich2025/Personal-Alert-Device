package com.example.personalalertdevice.Health

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush

interface ClinicalTablesService {
    @GET("conditions/v3/search")
    fun searchConditions(
        @Query("terms") query: String,
        @Query("maxList") maxResults: Int = 10
    ): Call<Array<Any>>
}

data class MedicalCondition(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoScreen(navController: NavController) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val firestore = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    val medicalViewModel: MedicalViewModel = viewModel(factory = MedicalViewModelFactory(firestore))

    val (allergies, setAllergies) = remember { mutableStateOf("") }
    val (medications, setMedications) = remember { mutableStateOf("") }
    val (donor, setDonor) = remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }

    var conditionSearchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var conditionSuggestions = remember { mutableStateListOf<String>() }
    val selectedConditions = remember { mutableStateListOf<MedicalCondition>() }

    val primaryColor = Color(0xFF558F4F)
    val secondaryColor = Color(0xFFEDE4E1)
    val accentColor = Color(0xFF3B5249)
    val cardBackgroundColor = Color(0xFFF8F4F2)

    val loggingInterceptor = remember {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    val client = remember {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // NIH Medical Conditions API
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://clinicaltables.nlm.nih.gov/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val clinicalTablesService = remember {
        retrofit.create(ClinicalTablesService::class.java)
    }

    val medicalData = medicalViewModel.medicalData.value

    LaunchedEffect(userId) {
        medicalViewModel.loadMedicalData(userId)
    }

    LaunchedEffect(medicalData) {
        medicalData?.let { data ->
            setAllergies(data["allergies"] ?: "")
            setMedications(data["medications"] ?: "")
            setDonor(data["donor"] ?: "")
            conditions = data["conditions"] ?: ""

            val savedConditions = conditions.split(",").filter { it.isNotBlank() }
            selectedConditions.clear()
            savedConditions.forEach {
                selectedConditions.add(MedicalCondition(it.trim()))
            }
        }
    }

    LaunchedEffect(conditionSearchQuery) {
        if (conditionSearchQuery.length >= 2) {
            isSearching = true
            delay(300)

            clinicalTablesService.searchConditions(conditionSearchQuery)
                .enqueue(object : Callback<Array<Any>> {
                    override fun onResponse(
                        call: Call<Array<Any>>,
                        response: Response<Array<Any>>
                    ) {
                        if (response.isSuccessful) {
                            val results = response.body()
                            results?.let {
                                try {
                                    if (it.size >= 4) {
                                        val conditionsArray = it[3]
                                        if (conditionsArray is ArrayList<*>) {
                                            val suggestions = mutableListOf<String>()

                                            for (item in conditionsArray) {
                                                if (item is ArrayList<*> && item.isNotEmpty()) {
                                                    val conditionName = item[0].toString()
                                                    suggestions.add(conditionName)
                                                }
                                            }

                                            conditionSuggestions.clear()
                                            conditionSuggestions.addAll(suggestions)

                                            Log.d("ConditionSearch", "Parsed suggestions: $suggestions")
                                        }
                                        else {

                                        }
                                    } else {
                                        Log.d(
                                            "ConditionSearch",
                                            "Invalid response format: ${it.joinToString()}"
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("ConditionSearch", "Error parsing response", e)
                                }
                            }
                        } else {
                            Log.e(
                                "ConditionSearch",
                                "API error: ${response.code()} - ${response.message()}"
                            )
                        }
                        isSearching = false
                    }

                    override fun onFailure(call: Call<Array<Any>>, t: Throwable) {
                        Log.e("ConditionSearch", "API call failed", t)
                        isSearching = false
                    }
                })
        } else {
            conditionSuggestions.clear()
        }
    }

    fun saveConditionsToFirebase() {
        val conditionsString = selectedConditions.joinToString(", ") { it.name }
        medicalViewModel.saveMedicalData(userId, allergies, medications, donor, conditionsString)
    }

    fun removeCondition(condition: MedicalCondition) {
        selectedConditions.remove(condition)
        // Update in Firestore immediately
        saveConditionsToFirebase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF5F8F5)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(
            onClick = {
                saveConditionsToFirebase()
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 34.dp, bottom = 16.dp, start = 16.dp)
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
                    text = "Previous Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Main content cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ALLERGIES CARD
            MedicalInfoCard(
                title = "Allergies",
                primaryColor = primaryColor
            ) {
                OutlinedTextField(
                    value = allergies,
                    onValueChange = setAllergies,
                    label = {
                        Text(
                            "List All Medically Relevant Allergies",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        containerColor = secondaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }

            // MEDICATIONS CARD
            MedicalInfoCard(
                title = "Medications",
                primaryColor = primaryColor
            ) {
                OutlinedTextField(
                    value = medications,
                    onValueChange = setMedications,
                    label = {
                        Text(
                            "List All Currently Taken Medications",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        containerColor = secondaryColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }

            // ORGAN DONOR CARD
            MedicalInfoCard(
                title = "Organ Donor Status",
                primaryColor = primaryColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf("Yes", "No").forEach { option ->
                        Button(
                            onClick = { setDonor(option) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (donor == option) primaryColor else secondaryColor,
                                contentColor = if (donor == option) Color.White else Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = option,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // MEDICAL CONDITIONS CARD
            MedicalInfoCard(
                title = "Medical Conditions",
                primaryColor = primaryColor
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Search field with icon
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = conditionSearchQuery,
                            onValueChange = { conditionSearchQuery = it },
                            label = {
                                Text(
                                    "Search For Medical Conditions",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            textStyle = TextStyle(fontSize = 18.sp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray,
                                containerColor = secondaryColor
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = primaryColor
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )

                        if (isSearching) {
                            Text(
                                "Searching...",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp, top = 64.dp),
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (conditionSuggestions.isEmpty() && conditionSearchQuery.length >= 2) {
                            Text(
                                "No suggestions found",
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp, top = 64.dp),
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (conditionSuggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp)
                                    .zIndex(10f),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    items(conditionSuggestions) { suggestion ->
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedConditions.add(MedicalCondition(suggestion))
                                                        conditionSearchQuery = ""
                                                        conditionSuggestions.clear()
                                                        saveConditionsToFirebase()
                                                    }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = suggestion,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Black,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Divider(color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SELECTED CONDITIONS LIST
                    if (selectedConditions.isNotEmpty()) {
                        Text(
                            text = "Selected Conditions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = cardBackgroundColor,
                            shadowElevation = 2.dp
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                items(selectedConditions) { condition ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = secondaryColor
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = condition.name,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 18.sp,
                                                color = Color.Black,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { removeCondition(condition) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove condition",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = cardBackgroundColor,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No medical conditions selected",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalInfoCard(
    title: String,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Colored indicator bar
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(32.dp)
                        .background(primaryColor, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}