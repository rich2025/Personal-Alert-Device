package com.example.personalalertdevice.Health

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MedicalViewModel(private val firestore: FirebaseFirestore) : ViewModel() {
    // State for profile data
    private val _medicalData = mutableStateOf<Map<String, String>?>(null)
    val medicalData: State<Map<String, String>?> get() = _medicalData

    // Save medical data to Firestore
    fun saveMedicalData(userId: String, allergies: String, medications: String) {
        val MedicalData = hashMapOf(
            "allergies" to allergies,
            "medications" to medications
        )

        firestore.collection("Users")
            .document(userId)
            .set(MedicalData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Medical data successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error writing medical data", e)
            }
    }

    // Load medical data from Firestore
    fun loadMedicalData(userId: String) {
        firestore.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    _medicalData.value = document.data as Map<String, String>
                } else {
                    Log.d("MedicalViewModel", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MedicalViewModel", "Error getting document: ", exception)
            }
    }
}

