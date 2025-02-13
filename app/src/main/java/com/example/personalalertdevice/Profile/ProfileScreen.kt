package com.example.personalalertdevice.Profile

import androidx.compose.foundation.clickable
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

    val (name, setName) = remember { mutableStateOf("") }
    val (birthday, setBirthday) = remember { mutableStateOf("") }
    val (age, setAge) = remember { mutableStateOf("") }
    val (gender, setGender) = remember { mutableStateOf("") }
    val (weight, setWeight) = remember { mutableStateOf("") }
    val (height, setHeight) = remember { mutableStateOf("") }
    val genders = listOf("Male", "Female", "Other")
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (address, setAddress) = remember { mutableStateOf("") }

    val (selectedMonth, setSelectedMonth) = remember { mutableStateOf("") }
    val (expandedMonth, setExpandedMonth) = remember { mutableStateOf(false) }

    val (selectedDay, setSelectedDay) = remember { mutableStateOf("") }
    val (expandedDay, setExpandedDay) = remember { mutableStateOf(false) }

    val (selectedYear, setSelectedYear) = remember { mutableStateOf("") }
    val (expandedYear, setExpandedYear) = remember { mutableStateOf(false) }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val days = (1..31).map { it.toString() }
    val years = (1900..2025).map { it.toString() }

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
                    label = {
                        Text(
                            "Click to Enter Your First and Last Name",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
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
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Month
                Box {
                    Button(
                        onClick = { setExpandedMonth(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(55.dp)
                            .width(150.dp)
                    ) {
                        Text(
                            text = selectedMonth.ifEmpty { "Month" },
                            fontSize = if (selectedMonth.isEmpty()) 15.sp else 22.sp,
                            fontWeight = if (selectedMonth.isEmpty()) FontWeight.SemiBold else FontWeight.Bold,
                            color = if (selectedMonth.isEmpty()) Color.DarkGray else Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { setExpandedMonth(false) }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = month,
                                        fontSize = if (month == selectedMonth) 22.sp else 15.sp,
                                        fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (month == selectedMonth) Color.Black else Color.DarkGray
                                    )
                                },
                                onClick = {
                                    setSelectedMonth(month)
                                    setExpandedMonth(false)
                                }
                            )
                        }
                    }
                }

                // Day
                Box {
                    Button(
                        onClick = { setExpandedDay(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(55.dp)
                            .width(90.dp)
                    ) {
                        Text(
                            text = selectedDay.ifEmpty { "Day" },
                            fontSize = if (selectedDay.isEmpty()) 15.sp else 22.sp,
                            fontWeight = if (selectedDay.isEmpty()) FontWeight.SemiBold else FontWeight.Bold,
                            color = if (selectedDay.isEmpty()) Color.DarkGray else Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { setExpandedDay(false) }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = day,
                                        fontSize = if (day == selectedDay) 22.sp else 15.sp,
                                        fontWeight = if (day == selectedDay) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (day == selectedDay) Color.Black else Color.DarkGray
                                    )
                                },
                                onClick = {
                                    setSelectedDay(day)
                                    setExpandedDay(false)
                                }
                            )
                        }
                    }
                }

                // Year
                Box {
                    Button(
                        onClick = { setExpandedYear(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(55.dp)
                            .width(120.dp)
                    ) {
                        Text(
                            text = selectedYear.ifEmpty { "Year" },
                            fontSize = if (selectedYear.isEmpty()) 15.sp else 22.sp,
                            fontWeight = if (selectedYear.isEmpty()) FontWeight.SemiBold else FontWeight.Bold,
                            color = if (selectedYear.isEmpty()) Color.DarkGray else Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { setExpandedYear(false) }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = year,
                                        fontSize = if (year == selectedYear) 22.sp else 15.sp,
                                        fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (year == selectedYear) Color.Black else Color.DarkGray
                                    )
                                },
                                onClick = {
                                    setSelectedYear(year)
                                    setExpandedYear(false)
                                }
                            )
                        }
                    }
                }
            }


                Spacer(modifier = Modifier.height(32.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        viewModel.saveProfileData(
                            userId,
                            name,
                            birthday,
                            age,
                            gender,
                            weight,
                            height,
                            address
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(60.dp)
                ) {
                    Text("Save Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
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

//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Save Profile Button
//        Button(
//            onClick = {
//                viewModel.saveProfileData(userId, name, birthday, age, gender, weight, height, address)
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 32.dp)
//                .height(60.dp)
//        ) {
//            Text("Save Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//    }
//}
