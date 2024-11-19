package com.example.personalalertdevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModelFactory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
