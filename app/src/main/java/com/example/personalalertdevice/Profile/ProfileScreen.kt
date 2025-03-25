package com.example.personalalertdevice.Profile

import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar
import java.time.Month
import java.time.format.DateTimeFormatter


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

    val (heightFeet, setHeightFeet) = remember { mutableStateOf("") }
    val (heightInches, setHeightInches) = remember { mutableStateOf("") }
    val (height, setHeight) = remember { mutableStateOf("") }
    val genders = listOf("Male", "Female", "Other")
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (address, setAddress) = remember { mutableStateOf("") }
    val (addressLine1, setAddressLine1) = remember { mutableStateOf("") }
    val (city, setCity) = remember { mutableStateOf("") }
    val (selectedState, setSelectedState) = remember { mutableStateOf("") }
    val (expandedState, setExpandedState) = remember { mutableStateOf(false) }
    val (zip, setZip) = remember { mutableStateOf("") }

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

    val states = listOf(
        "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware",
        "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky",
        "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi",
        "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico",
        "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania",
        "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont",
        "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"
    )

    val days = (1..31).map { it.toString() }
    val years = (1920..2025).map { it.toString() }

    LaunchedEffect(userId) {
        viewModel.loadProfileData(userId)
    }

    val profileData = viewModel.profileData.value
    LaunchedEffect(profileData) {
        profileData?.let { data ->
            setName(data["full name"] ?: "")
            val birthdayStr = data["birthday"] ?: ""
            if (birthdayStr.isNotEmpty()) {
                val parts = birthdayStr.split(" ")
                if (parts.size == 3) {
                    setSelectedMonth(parts[0])
                    setSelectedDay(parts[1].replace(",", ""))
                    setSelectedYear(parts[2])
                    setExpandedMonth(false)
                    setExpandedDay(false)
                    setExpandedYear(false)
                } else {
                    setSelectedMonth("")
                    setSelectedDay("")
                    setSelectedYear("")
                    setExpandedMonth(false)
                    setExpandedDay(false)
                    setExpandedYear(false)
                }
            } else {
                setSelectedMonth("")
                setSelectedDay("")
                setSelectedYear("")
                setExpandedMonth(false)
                setExpandedDay(false)
                setExpandedYear(false)
            }
            setAge(data["age"] ?: "")
            setGender(data["gender"] ?: "")
            setWeight(data["weight"] ?: "")
            val heightStr = data["height"] ?: ""
            if (heightStr.isNotEmpty() && heightStr.contains("'")) {
                val parts = heightStr.split("'")
                if (parts.size == 2) {
                    setHeightFeet(parts[0].trim())
                    setHeightInches(parts[1].replace("\"", "").trim())
                } else {
                    setHeightFeet("")
                    setHeightInches("")
                }
            } else {
                setHeightFeet("")
                setHeightInches("")
            }
            setAddress("8 St Mary's St, Boston")
        }
    }

    fun calculateAge() {
        if (selectedMonth.isNotEmpty() && selectedDay.isNotEmpty() && selectedYear.isNotEmpty()) {
            try {
                val birthCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear.toInt())
                    set(Calendar.MONTH, Month.valueOf(selectedMonth.uppercase()).ordinal)
                    set(Calendar.DAY_OF_MONTH, selectedDay.toInt())
                }

                val currentCalendar = Calendar.getInstance()
                var calculatedAge = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

                if (currentCalendar.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                    (currentCalendar.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                            currentCalendar.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                    calculatedAge -= 1
                }

                setAge(calculatedAge.toString())
            } catch (e: Exception) {
                setAge("")
            }
        }
    }

    LaunchedEffect(selectedMonth, selectedDay, selectedYear) {
        calculateAge()
        if (selectedMonth.isNotEmpty() || selectedDay.isNotEmpty() || selectedYear.isNotEmpty()) {
            setBirthday("$selectedMonth $selectedDay, $selectedYear")
        }
    }

    LaunchedEffect(heightFeet, heightInches) {
        if (heightFeet.isNotEmpty() || heightInches.isNotEmpty()) {
            setHeight("$heightFeet' $heightInches\"")
        }
    }


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

            // GENDER INPUT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Gender",
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
                    listOf("Male", "Female", "Other").forEach { option ->
                        Button(
                            onClick = { setGender(option) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xffede4e1)
                            ),
                            modifier = Modifier
                                .width(120.dp)
                                .height(55.dp)
                                .then(
                                    if (gender == option) Modifier.border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    ) else Modifier
                                )
                        ) {
                            Text(
                                text = option,
                                fontSize = if (gender == option) 22.sp else 15.sp,
                                fontWeight = if (gender == option) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (gender == option) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // WEIGHT AND HEIGHT INPUT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Weight and Height",
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Weight
                    TextField(
                        value = weight,
                        onValueChange = setWeight,
                        label = {
                            Text(
                                "Weight (lbs)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(55.dp)
                    )

                    // Height in feet
                    TextField(
                        value = heightFeet,
                        onValueChange = setHeightFeet,
                        label = {
                            Text(
                                "Height (ft)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(55.dp)
                    )

                    // Height in inches
                    TextField(
                        value = heightInches,
                        onValueChange = setHeightInches,
                        label = {
                            Text(
                                "Height (in)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xffede4e1)
                        ),
                        modifier = Modifier
                            .width(110.dp)
                            .height(55.dp)
                    )
                }
            }

//// ADDRESS
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//            ) {
//                Text(
//                    text = "Home Address",
//                    fontSize = 25.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .width(500.dp)
//                        .padding(bottom = 4.dp)
//                        .drawBehind {
//                            val strokeWidth = 2f
//                            val y = size.height - strokeWidth
//                            drawLine(
//                                color = Color.Black,
//                                start = Offset(0f, y),
//                                end = Offset(size.width, y),
//                                strokeWidth = strokeWidth
//                            )
//                        }
//                )
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 12.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    TextField(
//                        value = addressLine1,
//                        onValueChange = setAddressLine1,
//                        label = {
//                            Text(
//                                "Click to Enter Your Home Address",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        },
//                        textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
//                        colors = TextFieldDefaults.textFieldColors(
//                            containerColor = Color(0xffede4e1)
//                        ),
//                        modifier = Modifier
//                            .width(500.dp)
//                            .height(55.dp)
//                    )
//                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 12.dp),
//                    horizontalArrangement = Arrangement.spacedBy(5.dp)
//                ) {
//                    // City Field
//                    TextField(
//                        value = city,
//                        onValueChange = setCity,
//                        label = {
//                            Text(
//                                "City",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        },
//                        textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
//                        colors = TextFieldDefaults.textFieldColors(
//                            containerColor = Color(0xffede4e1)
//                        ),
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(55.dp)
//                    )
//
//                    // State Dropdown
//                    Box {
//                        Button(
//                            onClick = { setExpandedState(true) },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xffede4e1)
//                            ),
//                            shape = RoundedCornerShape(8.dp),
//                            modifier = Modifier
//                                .height(55.dp)
//                                .width(150.dp)
//                        ) {
//                            Text(
//                                text = selectedState.ifEmpty { "State" },
//                                fontSize = if (selectedState.isEmpty()) 15.sp else 22.sp,
//                                fontWeight = if (selectedState.isEmpty()) FontWeight.SemiBold else FontWeight.Bold,
//                                color = if (selectedState.isEmpty()) Color.DarkGray else Color.Black
//                            )
//                        }
//                        DropdownMenu(
//                            expanded = expandedState,
//                            onDismissRequest = { setExpandedState(false) }
//                        ) {
//                            states.forEach { state ->
//                                DropdownMenuItem(
//                                    text = {
//                                        Text(
//                                            text = state,
//                                            fontSize = if (state == selectedState) 22.sp else 15.sp,
//                                            fontWeight = if (state == selectedState) FontWeight.Bold else FontWeight.SemiBold,
//                                            color = if (state == selectedState) Color.Black else Color.DarkGray
//                                        )
//                                    },
//                                    onClick = {
//                                        setSelectedState(state)
//                                        setExpandedState(false)
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    // Zip Code Field
//                    TextField(
//                        value = zip,
//                        onValueChange = setZip,
//                        label = {
//                            Text(
//                                "Zip Code",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                        },
//                        textStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
//                        colors = TextFieldDefaults.textFieldColors(
//                            containerColor = Color(0xffede4e1)
//                        ),
//                        modifier = Modifier
//                            .width(150.dp)
//                            .height(55.dp)
//                    )
//                }
//            }


            Spacer(modifier = Modifier.height(32.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        setHeight("$heightFeet' $heightInches\"")
                        setBirthday("$selectedMonth $selectedDay, $selectedYear")

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
                        .height(60.dp)
                        .width(150.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558f4f))
                ) {
                    Text("Click to Save Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
