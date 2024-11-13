package com.example.personalalertdevice

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Class to hold user data from Authentication
data class User(
    val name: String = "",
    val email: String = "",
    val uid: String = ""
)

class UserRepository {

    private val db = Firebase.firestore

    suspend fun saveUserToFirestore(): Result<Unit> {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        return try {
            if (firebaseUser != null) {
                val user = User(
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "No Email",
                    uid = firebaseUser.uid
                )

                // Save the user data in Firestore
                db.collection("Users").document(firebaseUser.uid).set(user).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}