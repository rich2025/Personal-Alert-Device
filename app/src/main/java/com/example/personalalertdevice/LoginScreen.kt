package com.example.personalalertdevice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    googleAuthClient: GoogleAuthClient
) {
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Check if the user is already signed in
    LaunchedEffect(Unit) {
        if (!googleAuthClient.isSignedIn()) {
            googleAuthClient.signOut() // Clear credentials if not
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "Welcome",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    isLoading = true
                    loginError = null
                    coroutineScope.launch {
                        val isSignedIn = googleAuthClient.signIn()
                        isLoading = false
                        if (isSignedIn) {
                            onLoginSuccess()
                        } else {
                            loginError = "Login failed. Please try again."
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32a852)),
            ) {
                Text(
                    text = if (isLoading) "Signing in..." else "Sign in with Google",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            loginError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

