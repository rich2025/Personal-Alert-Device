package com.example.personalalertdevice.Profile

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfilePictureViewModel(private val firestore: FirebaseFirestore) : ViewModel() {
    private val _profileImageUrl = mutableStateOf<String?>(null)
    val profileImageUrl: State<String?> get() = _profileImageUrl

    fun loadProfileImage(userId: String) {
        firestore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("profile image")) {
                    _profileImageUrl.value = document.getString("profile image")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error loading profile image", e)
            }
    }

    fun saveProfileImage(userId: String, imageUrl: String) {
        val profileData = hashMapOf("profile image" to imageUrl)

        firestore.collection("Users")
            .document(userId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                // Update the profile image in the ViewModel immediately after saving
                _profileImageUrl.value = imageUrl
                Log.d("Firestore", "Profile image successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating profile image", e)
            }
    }
}



