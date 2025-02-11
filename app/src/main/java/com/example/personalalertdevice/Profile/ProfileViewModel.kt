package com.example.personalalertdevice.Profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileViewModel(private val firestore: FirebaseFirestore) : ViewModel() {
    fun saveProfileData(userId: String, name: String, birthday: String, age: String, gender: String, weight: String, height: String, address: String) {
        val profileData = hashMapOf(
            "full name" to name,
            "birthday" to birthday,
            "age" to age,
            "gender" to gender,
            "weight" to weight,
            "height" to height,
            "address" to address
        )

        firestore.collection("Users")
            .document(userId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Profile data successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing profile data", e)
            }
    }
}
