package com.example.personalalertdevice.Health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class MedicalViewModelFactory(private val firestore: FirebaseFirestore) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalViewModel::class.java)) {
            return MedicalViewModel(firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}