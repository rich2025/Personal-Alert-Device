package com.example.personalalertdevice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Welcome, *NAME*",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF333333),
            modifier = Modifier.padding(vertical = 100.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {  },
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(text = "Button 1")
                }
                Button(
                    onClick = {  },
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(text = "Button 2")
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {  },
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(text = "Button 3")
                }
                Button(
                    onClick = {  },
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(text = "Button 4")
                }
            }
        }
    }
}
