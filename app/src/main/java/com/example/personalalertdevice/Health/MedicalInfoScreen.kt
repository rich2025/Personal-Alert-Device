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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.RectangleShape

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

    val medicalViewModel: MedicalViewModel = viewModel(factory = MedicalViewModelFactory(firestore))

    val (allergies, setAllergies) = remember { mutableStateOf("") }
    val (medications, setMedications) = remember { mutableStateOf("") }
    val (donor, setDonor) = remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }

    var conditionSearchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var conditionSuggestions = remember { mutableStateListOf<String>() }
    val selectedConditions = remember { mutableStateListOf<MedicalCondition>() }

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
                                    // [0] = status code (91.0)
                                    // [1] = array of IDs
                                    // [2] = null
                                    // [3] = array of arrays

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
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Return Button
        Button(
            onClick = {
                saveConditionsToFirebase()
                navController.popBackStack()
            },
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
                    text = "Previous Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        // ALLERGIES
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Allergies",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(500.dp)
                    .padding(bottom = 4.dp)
                    .drawBehind {
                        val strokeWidth = 2f
                        val y = size.height - strokeWidth
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = allergies,
                    onValueChange = setAllergies,
                    label = {
                        Text(
                            "List All Medically Relevant Allergies",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xffede4e1)
                    ),
                    modifier = Modifier
                        .width(500.dp)
                        .height(55.dp)
                )
            }
        }

        // MEDICATIONS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Medications",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(500.dp)
                    .padding(bottom = 4.dp)
                    .drawBehind {
                        val strokeWidth = 2f
                        val y = size.height - strokeWidth
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = medications,
                    onValueChange = setMedications,
                    label = {
                        Text(
                            "List All Currently Taken Medications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xffede4e1)
                    ),
                    modifier = Modifier
                        .width(500.dp)
                        .height(55.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Organ Donor Status",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(500.dp)
                        .padding(bottom = 4.dp)
                        .drawBehind {
                            val strokeWidth = 2f
                            val y = size.height - strokeWidth
                            drawLine(
                                color = Color.Black,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Yes", "No").forEach { option ->
                        Button(
                            onClick = { setDonor(option) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xffede4e1)
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .height(55.dp)
                                .then(
                                    if (donor == option) Modifier.border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    ) else Modifier
                                )
                        ) {
                            Text(
                                text = option,
                                fontSize = if (donor == option) 22.sp else 15.sp,
                                fontWeight = if (donor == option) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (donor == option) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // MEDICAL CONDITIONS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Medical Conditions",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(500.dp)
                        .padding(bottom = 4.dp)
                        .drawBehind {
                            val strokeWidth = 2f
                            val y = size.height - strokeWidth
                            drawLine(
                                color = Color.Black,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    TextField(
                        value = conditionSearchQuery,
                        onValueChange = { conditionSearchQuery = it },
                        label = {
                            Text(
                                "Search For Medical Conditions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        modifier = Modifier
                            .width(500.dp)
                            .height(55.dp)
                    )

                    if (isSearching) {
                        Text(
                            "Searching...",
                            modifier = Modifier.padding(top = 60.dp),
                            color = Color(0xFF558f4f),
                            fontWeight = FontWeight.Bold
                        )
                    } else if (conditionSuggestions.isEmpty() && conditionSearchQuery.length >= 2) {
                        Text(
                            "No suggestions found",
                            modifier = Modifier.padding(top = 60.dp),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (conditionSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .width(500.dp)
                                .padding(top = 60.dp)
                                .zIndex(10f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth()
                                    .border(2.dp, Color.Gray)
                                    .background(Color.White)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(conditionSuggestions) { suggestion ->
                                        Text(
                                            text = suggestion,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedConditions.add(MedicalCondition(suggestion))
                                                    conditionSearchQuery = ""
                                                    conditionSuggestions.clear()
                                                    saveConditionsToFirebase()
                                                }
                                                .padding(4.dp)
                                                .background(Color(0xFFF5F5F5))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SELECTED CONDITIONS LIST SECTION
                if (selectedConditions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selected Conditions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5),
                        shadowElevation = 2.dp
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            items(selectedConditions) { condition ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                        .background(
                                            color = Color(0xFFE6E6E6),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = condition.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove condition",
                                        tint = Color.Red,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                removeCondition(condition)
                                            }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No medical conditions selected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}