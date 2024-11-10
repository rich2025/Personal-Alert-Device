package com.example.personalalertdevice

import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personalalertdevice.sign_in.DummySignInScreen
import com.example.personalalertdevice.sign_in.GoogleAuthUiClient
import com.example.personalalertdevice.sign_in.SignInScreen
import com.example.personalalertdevice.sign_in.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.material3.Button
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private val auth by lazy { Firebase.auth } // Add this line to initialize FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "sign_in") {
                composable(route = "sign_in") {
                    val viewModel: SignInViewModel = viewModel()
                    val state = viewModel.state.collectAsStateWithLifecycle().value

                    LaunchedEffect(key1 = Unit) {
                        if (googleAuthUiClient.getSignedInUser() != null)
                        {
                            navController.navigate("MainScreen") //change to profile if you want to test
                        }
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = { result ->
                            if (result.resultCode == RESULT_OK) {
                                lifecycleScope.launch {
                                    val signInResult = googleAuthUiClient.signInWithIntent(
                                        intent = result.data ?: return@launch
                                    )
                                    viewModel.onSignInResult(signInResult)
                                }
                            }
                        }
                    )

                    LaunchedEffect(key1 = state.isSignInSuccessful) {
                        if (state.isSignInSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Sign In Successful",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("MainScreen") //you can change this to profile if you want to test
                            viewModel.resetState()
                        }
                    }

                    SignInScreen(
                        state = state,
                        onSignInClick = {
                           lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                  ).build()
                                )
                           }
                        }
                    )
                    Button(
                        onClick = { navController.navigate("dummy_sign_in") }
                    ) {
                        Text(text = "Go to Dummy Sign In")
                    }
                }

                composable(route = "dummy_sign_in") {
                    DummySignInScreen(navController = navController)
                }

                // Other composable screens, uncomment below if you want to test Sign In button redirection
                //composable(route = "profile")
                //{ ProfileScreenV2(
                  //  userData = googleAuthUiClient.getSignedInUser(),
                    //onSignOut = {
                      //  lifecycleScope.launch{
                        //    googleAuthUiClient.signOut()
                          //  Toast.makeText(
                            //    applicationContext,
                              //  "Signed Out",
                               // Toast.LENGTH_LONG
                          //  ).show()
                           // navController.popBackStack()
                       // }
                    //}
                //)
                //}

                composable(route = "MainScreen") {MainScreen(navController)}
                composable(route = "ProfileScreen") { ProfileScreen(navController) }
                composable(route = "HealthScreen") { HealthScreen(navController) }
                composable(route = "ContactsScreen") { ContactsScreen(navController) }
                composable(route = "HowToScreen") { HowToScreen(navController) }
                composable(route = "HelpScreen") { HelpScreen(navController) }
                composable(route = "ContactsListScreen") { ContactsListScreen(navController) }
            }
        }
    }
}
