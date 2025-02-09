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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.personalalertdevice.Profile.ProfilePictureViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Composable for main screen
@Composable
fun MainScreen(
    navController: NavController,
    userName: String,
    profilePictureUrl: String?,
    viewModel: ProfilePictureViewModel,
    userId: String
) {
    LaunchedEffect(userId) {
        viewModel.loadProfileImage(userId)
    }

    val uploadedProfilePictureUrl = viewModel.profileImageUrl.value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sign Out button
        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate("LoginScreen") {
                    popUpTo("MainScreen") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .width(200.dp)
                .height(55.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffde4526))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                if (profilePictureUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Retrieve user name and profile picture
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.padding(vertical = 70.dp)
                    .offset(y = 17.dp)
            )

            if (uploadedProfilePictureUrl != null) {
                AsyncImage(
                    model = uploadedProfilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(170.dp)
                        .offset(y = (-20).dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(170.dp)
                        .offset(y = (-20).dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Navigation Button Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
                    .offset(y = -15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            navController.navigate("ProfileScreenMain")
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
                                    fontSize = 43.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
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
                                modifier = Modifier.size(57.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "Health",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 43.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
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
                                text = "Contact",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black,
                                maxLines = 1
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
                                    fontSize = 37.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
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
}

    // Composable for holdable button
    @Composable
    fun HoldButtonWithProgress(
        onCompleted: () -> Unit,
        modifier: Modifier = Modifier,
        holdDuration: Long = 20000L,
        progressColor: Color = Color(0xFF8B0000),
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

