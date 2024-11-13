package com.example.personalalertdevice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        googleAuthClient = GoogleAuthClient(this)

        setContent {
            val navController = rememberNavController()
            val contactsViewModel = viewModel<ContactsViewModel>()

        // GOOGLE AUTH CLIENT
            // Check if user previously signed in
            val startDestination = if (googleAuthClient.isSignedIn()) {
                "MainScreen"  // If user already logged in, go to MainScreen
            } else {
                "LoginScreen" // Else go to LoginScreen
            }

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val fullName = firebaseUser?.displayName ?: "User"
            val userName = fullName.split(" ").firstOrNull() ?: "User"
            val profilePictureUrl = firebaseUser?.photoUrl?.toString()

            NavHost(navController = navController, startDestination = startDestination) {
                composable("MainScreen") {
                    MainScreen(
                        navController = navController,
                        userName = userName,
                        profilePictureUrl = profilePictureUrl
                    )
                }
                composable("LoginScreen") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("MainScreen")
                        },
                        googleAuthClient = googleAuthClient
                    )
                }
        // GOOGLE AUTH CLIENT

                composable("ProfileScreen") { ProfileScreen(navController) }
                composable("HealthScreen") { HealthScreen(navController) }
                composable("ContactsScreen") { ContactsScreen(navController) }
                composable("HowToScreen") { HowToScreen(navController) }
                composable("HelpScreen") { HelpScreen(navController) }
                composable("ContactsListScreen") {
                    ContactsListScreen(navController, contactsViewModel)
                }
            }
        }
    }
}
