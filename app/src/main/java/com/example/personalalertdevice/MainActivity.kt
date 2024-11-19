package com.example.personalalertdevice

import ContactsViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.example.personalalertdevice.saveUserData
import com.example.personalalertdevice.loadUserData

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthClient: GoogleAuthClient
    private val userRepository = UserRepository()
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

    private var userName: String = "User"
    private var profilePictureUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        googleAuthClient = GoogleAuthClient(this)

        // Load user data from UserDataDetails shared preference
        val (savedUserName, savedProfilePictureUrl) = loadUserData(this)
        userName = savedUserName
        profilePictureUrl = savedProfilePictureUrl

        setContent {
            val navController = rememberNavController()
            val contactsViewModel = viewModel<ContactsViewModel>()

            val startDestination = if (googleAuthClient.isSignedIn()) {
                "MainScreen"
            } else {
                "LoginScreen"
            }

            // Listen for authentication state changes
            firebaseAuth.addAuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val fullName = firebaseUser.displayName ?: "User"
                    userName = fullName.split(" ").firstOrNull() ?: "User"
                    profilePictureUrl = firebaseUser.photoUrl?.toString()

                    // save user data to the shared preference
                    saveUserData(this@MainActivity, userName, profilePictureUrl)

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
                            navController.navigate("MainScreen")
                        },
                        googleAuthClient = googleAuthClient
                    )
                }
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
