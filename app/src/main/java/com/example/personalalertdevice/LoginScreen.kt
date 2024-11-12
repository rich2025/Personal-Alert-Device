package com.example.personalalertdevice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Icon",
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = 10.dp, y = (-10).dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height((0).dp))
            val coroutineScope = rememberCoroutineScope()

            // Button Box
            Box() {
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
                                loginError = "     LOGIN FAILED\nPLEASE TRY AGAIN"
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .offset(y = (-100).dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32a852)),
                ) {
                    Text(
                        text = if (isLoading) "Signing in..." else "Sign in with Google",
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Spacer to move the error message below the button
            Spacer(modifier = Modifier.offset(y = (300).dp)) // Adjust to control spacing below the button

            // Error message displayed below the button
            loginError?.let { error ->
                Text(
                    text = error,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}
