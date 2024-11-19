package com.example.personalalertdevice

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(private val firestore: FirebaseFirestore) : ViewModel() {
    fun saveProfileData(userId: String, age: String, gender: String, weight: String, height: String) {
        val profileData = hashMapOf(
            "age" to age,
            "gender" to gender,
            "weight" to weight,
            "height" to height
        )

        firestore.collection("Users")
            .document(userId)
            .set(profileData)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile data successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing profile data", e)
            }
    }
}
