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
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF333333),
            modifier = Modifier.padding(vertical = 100.dp)
        )

        // Navigation Button Group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333)
                    )
                }
                Button(
                    onClick = { },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text(
                        text = "Health",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                ) {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333)
                    )
                }
                Button(
                    onClick = { },
                    modifier = Modifier.size(175.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe9e332))
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 31.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF333333)
                    )
                }
            }
            HoldButtonWithProgress(
                onCompleted = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(20.dp)
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
                            while (isPressed && progress < 1f) {
                                delay(10)
                                progress += 0.01f * (10f / holdDuration * 1000f)
                            }
                            if (progress >= 1f) {
                                onCompleted()
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        scope.launch {
                            while (!isPressed && progress > 0f) {
                                delay(10)
                                progress -= 0.05f * (10f / holdDuration * 1000f)
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
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(progressColor) // Dark red progress bar
            )
            Text(
                text = "HOLD FOR HELP",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}
