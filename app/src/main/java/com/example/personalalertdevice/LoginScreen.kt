package com.example.personalalertdevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController


@Composable
fun LoginScreen(navController: NavController) {

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f4e4)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login Screen")
        Button(onClick = {
            navController.navigate("MainScreen")
        }) {
            Text(text = "Dummy Login Button")
        }
    }
}