package com.example.personalalertdevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Create ViewModel using the custom factory
    val factory = ProfileViewModelFactory(firestore)
    val viewModel: ProfileViewModel = viewModel(factory = factory)

    // UI remains the same
    val (age, setAge) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (weight, setWeight) = remember { mutableStateOf("") }
    val (height, setHeight) = remember { mutableStateOf("") }
    val genders = listOf("Male", "Female", "Other")
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
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
            Text(
                text = "RETURN",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Age Input
        TextField(
            value = age,
            onValueChange = setAge,
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Gender Dropdown
        Text(
            text = "Gender",
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Box {
            Button(onClick = { setExpanded(true) }) {
                Text(text = gender.ifEmpty { "Select Gender" })
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
                genders.forEach { genderOption ->
                    DropdownMenuItem(
                        text = { Text(genderOption) },
                        onClick = {
                            setGender(genderOption)
                            setExpanded(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weight Input
        TextField(
            value = weight,
            onValueChange = setWeight,
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Height Input
        TextField(
            value = height,
            onValueChange = setHeight,
            label = { Text("Height (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Profile Button
        Button(
            onClick = {
                viewModel.saveProfileData(userId, age, gender, weight, height)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}
