package com.example.personalalertdevice

import com.example.personalalertdevice.Contacts.ContactsViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.ViewModelProvider
import com.example.personalalertdevice.Contacts.ContactsListScreen
import com.example.personalalertdevice.Contacts.ContactsScreen
import com.example.personalalertdevice.Health.HealthScreen
import com.example.personalalertdevice.Health.VitalsScreen
import com.example.personalalertdevice.Profile.ProfilePictureViewModel
import com.example.personalalertdevice.Profile.ProfilePictureViewModelFactory
import com.example.personalalertdevice.Profile.ProfileScreenMain
import com.example.personalalertdevice.Profile.ProfileScreen
import com.example.personalalertdevice.Profile.ProfileViewModel
import com.example.personalalertdevice.Profile.ProfileViewModelFactory

import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var googleAuthClient: GoogleAuthClient
    private val userRepository = UserRepository()
    private val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

    private var userName: String = "User"
    private var profilePictureUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialize FirebaseFirestore
        val firestore = FirebaseFirestore.getInstance()

        //use custom factory to create ProfileViewModel
        val factory = ProfileViewModelFactory(firestore)
        profileViewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)

        googleAuthClient = GoogleAuthClient(this)

        // Load user data from UserDataDetails shared preference
        val (savedUserName, savedProfilePictureUrl) = loadUserData(this)
        userName = savedUserName
        profilePictureUrl = savedProfilePictureUrl



        setContent {
            val navController = rememberNavController()
            val contactsViewModel = viewModel<ContactsViewModel>()
            val profilePictureViewModel: ProfilePictureViewModel = viewModel(factory = ProfilePictureViewModelFactory(firestore))
            val profileImageUrl = profilePictureViewModel.profileImageUrl.value
            val startDestination = if (googleAuthClient.isSignedIn()) {
                "MainScreen"
            } else {
                "LoginScreen"
            }
            val userId = firebaseAuth.currentUser?.uid ?: ""
            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) {
                    profilePictureViewModel.loadProfileImage(userId)
                }
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
                val userId = firebaseAuth.currentUser?.uid ?: ""

                composable("MainScreen") {
                    MainScreen(
                        navController = navController,
                        userName = userName,
                        profilePictureUrl = profilePictureUrl,
                        viewModel = profilePictureViewModel,
                        userId = userId
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
                composable("ProfileScreenMain") {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    ProfileScreenMain(navController = navController, userId = userId)
                }
                composable("ProfileScreen") {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    ProfileScreen(navController = navController, userId = userId)
                }
                composable("HealthScreen") { HealthScreen(navController) }
                composable("ContactsScreen") { ContactsScreen(navController, contactsViewModel) }
                composable("HowToScreen") { HowToScreen(navController) }
                composable("HelpScreen") { HelpScreen(navController) }
                composable("VitalsScreen") { VitalsScreen(navController) }
                composable("ContactsListScreen") {
                    ContactsListScreen(navController, contactsViewModel)
                }
            }
        }
    }
}
