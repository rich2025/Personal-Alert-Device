package com.example.personalalertdevice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
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
    private val userRepository = UserRepository()
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

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

            var userName = "User"
            var profilePictureUrl: String? = null

            firebaseAuth.addAuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val fullName = firebaseUser.displayName ?: "User"
                    userName = fullName.split(" ").firstOrNull() ?: "User"
                    profilePictureUrl = firebaseUser.photoUrl?.toString()

                    // Save user data to Firestore after login, if not already saved
                    lifecycleScope.launch {
                        val result = userRepository.saveUserToFirestore()
                        if (result.isFailure) {
                            Log.e("FirestoreError", "Failed to save user data: ${result.exceptionOrNull()}")
                        }
                    }
                }
            }

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
                            // Redirect to MainScreen after login success
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
