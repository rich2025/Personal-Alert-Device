package com.example.personalalertdevice.Profile

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileViewModel(private val firestore: FirebaseFirestore) : ViewModel() {
    // State for profile data
    private val _profileData = mutableStateOf<Map<String, String>?>(null)
    val profileData: State<Map<String, String>?> get() = _profileData

    // Save profile data to Firestore
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

    // Load profile data from Firestore
    fun loadProfileData(userId: String) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    _profileData.value = document.data as Map<String, String>
                } else {
                    Log.d("ProfileViewModel", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileViewModel", "Error getting document: ", exception)
            }
    }
}

