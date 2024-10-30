package com.example.personalalertdevice

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f4e4)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Hello, *NAME*",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black, // Changed to black
            modifier = Modifier.padding(vertical = 70.dp)
        )

        // Navigation Button Group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("ProfileScreen")
                    },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.profile_icon),
                            contentDescription = "Profile Icon",
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 31.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black // Changed to black
                        )
                    }
                }
                Button(
                    onClick = {
                        navController.navigate("HealthScreen")
                    },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.health_icon),
                            contentDescription = "Health Icon",
                            modifier = Modifier.size(55.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Health",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 31.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black // Changed to black
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("ContactsScreen")
                    },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.contacts_icon),
                            contentDescription = "Contacts Icon",
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 31.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black // Changed to black
                        )
                    }
                }
                Button(
                    onClick = {
                        navController.navigate("HowToScreen")
                    },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd7c0ed))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.how_to_icon),
                            contentDescription = "How To Icon",
                            modifier = Modifier.size(63.dp),
                            tint = Color.White
                        )
                        Text(
                            text = "How To",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 31.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black // Changed to black
                        )
                    }
                }
            }
            HoldButtonWithProgress(
                onCompleted = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun HoldButtonWithProgress(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    holdDuration: Long = 20000L,
    progressColor: Color = Color(0xFF8B0000), // Darker red
    backgroundColor: Color = Color.Red
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")
    var isPressed by remember { mutableStateOf(false) }
    var helpCalled by remember { mutableStateOf(false) } // Track if help was called
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        scope.launch {
                            while (isPressed && progress < 1f && !helpCalled) {
                                delay(10)
                                progress += 0.01f * (10f / holdDuration * 1000f)
                            }
                            if (progress >= 1f) {
                                helpCalled = true // Mark as help called
                                onCompleted()
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        scope.launch {
                            if (!helpCalled) {
                                while (!isPressed && progress > 0f) {
                                    delay(10)
                                    progress -= 0.05f * (10f / holdDuration * 1000f)
                                }
                            }
                        }
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            // Progress bar box
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(progressColor)
            )

            Text(
                text = if (helpCalled) "HELP CALLED" else "HOLD FOR HELP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}
