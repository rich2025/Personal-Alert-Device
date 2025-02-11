package com.example.personalalertdevice.Profile

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
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
//    val (firstName, setFirstName) = remember { mutableStateOf("") }
//    val (middleInitial, setMiddleInitial) = remember { mutableStateOf("") }
//    val (lastName, setLastName) = remember { mutableStateOf("") }
//
//    val name = listOf(firstName, middleInitial, lastName)
//        .filter { it.isNotBlank() }
//        .joinToString(" ")

    val (name, setName) = remember { mutableStateOf("") }
    val (birthday, setBirthday) = remember { mutableStateOf("") }
    val (age, setAge) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (weight, setWeight) = remember { mutableStateOf("") }
    val (height, setHeight) = remember { mutableStateOf("") }
    val genders = listOf("Male", "Female", "Other")
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (address, setAddress) = remember { mutableStateOf("") }



    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Return Button
        Button(
            onClick = { navController.navigate("ProfileScreenMain") },
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
                    text = "Profile Screen", // profile screen main
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        // NAME INPUT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Full Name",
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
                    value = name,
                    onValueChange = setName,
                    label = { Text("Click to Enter Your First and Last Name", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xffede4e1)
                    ),
                    modifier = Modifier
                        .width(500.dp)
                        .height(55.dp)
                )

            }
        }

        // BIRTHDAY INPUT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Birthday",
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
                    value = name,
                    onValueChange = setName,
                    label = { Text("Click to Choose Your Date of Birth", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xffede4e1)
                    ),
                    modifier = Modifier
                        .width(500.dp)
                        .height(55.dp)
                )

            }
        }


//        // Age Input
//        TextField(
//            value = age,
//            onValueChange = setAge,
//            label = { Text("Age", fontSize = 18.sp) },
//            textStyle = TextStyle(fontSize = 20.sp),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp)
//        )
//
//        // Gender Dropdown
//        Text(
//            text = "Gender",
//            fontSize = 18.sp,
//            modifier = Modifier.fillMaxWidth().padding(12.dp)
//        )
//        Box {
//            Button(
//                onClick = { setExpanded(true) },
//                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
//            ) {
//                Text(text = gender.ifEmpty { "Select Gender" }, fontSize = 18.sp)
//            }
//            DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
//                genders.forEach { genderOption ->
//                    DropdownMenuItem(
//                        text = { Text(genderOption, fontSize = 18.sp) },
//                        onClick = {
//                            setGender(genderOption)
//                            setExpanded(false)
//                        }
//                    )
//                }
//            }
//        }
//
//        // Weight
//        TextField(
//            value = weight,
//            onValueChange = setWeight,
//            label = { Text("Weight (kg)", fontSize = 18.sp) },
//            textStyle = TextStyle(fontSize = 20.sp),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp)
//        )
//
//        // Height Input
//        TextField(
//            value = height,
//            onValueChange = setHeight,
//            label = { Text("Height (cm)", fontSize = 18.sp) },
//            textStyle = TextStyle(fontSize = 20.sp),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp)
//        )
//
//        TextField(
//            value = address,
//            onValueChange = setAddress,
//            label = { Text("Address", fontSize = 18.sp) }, // Larger label
//            textStyle = TextStyle(fontSize = 20.sp), // Larger text
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 12.dp) // Added padding for better spacing
//        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Profile Button
        Button(
            onClick = {
                viewModel.saveProfileData(userId, name, birthday, age, gender, weight, height, address)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(60.dp)
        ) {
            Text("Save Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
