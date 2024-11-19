package com.example.personalalertdevice

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
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
                val user = mapOf(
                    "name" to (firebaseUser.displayName ?: "User"),
                    "email" to (firebaseUser.email ?: "No Email"),
                    "uid" to firebaseUser.uid
                )

                // Merge the user data in Firestore to preserve existing fields
                db.collection("Users")
                    .document(firebaseUser.uid)
                    .set(user, SetOptions.merge())
                    .await()

                Result.success(Unit)
            } else {
                Result.failure(Exception("No authenticated user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
